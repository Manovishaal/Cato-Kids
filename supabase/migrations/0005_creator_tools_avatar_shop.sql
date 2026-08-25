-- ============================================================
-- Cato Kids — creator tools (homework / activities / extra courses /
-- custom games), assignment submissions, and the student avatar + shop.
-- ============================================================

-- ---------- creator role helper ----------
create or replace function public.cato_is_creator()
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select coalesce((select role from public.profiles where id = auth.uid()) in ('teacher','school','admin'), false);
$$;

-- Symmetric "same school" check any signed-in profile can use (cato_same_school
-- only returns true for staff callers, which is right for reading a child's own
-- records but wrong for "can this student browse the school's shared library").
create or replace function public.cato_my_school(target_school uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (
    select 1 from public.profiles
    where id = auth.uid() and target_school is not null and school_id = target_school
  );
$$;

-- ---------- custom games (teacher/school/admin-authored) ----------
create table if not exists public.custom_games (
  id           uuid primary key default gen_random_uuid(),
  created_by   uuid references public.profiles(id) on delete set null,
  school_id    uuid references public.schools(id) on delete set null,
  title        text not null,
  description  text,
  subject_id   text references public.subjects(id) on delete set null,
  grade        public.grade_level,
  game_type    text not null,
  content      jsonb not null default '{}'::jsonb,
  is_published boolean not null default true,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);
create index if not exists custom_games_school_idx on public.custom_games(school_id);
create index if not exists custom_games_creator_idx on public.custom_games(created_by);

-- A student may only read a custom game through an assignment that puts them in it.
create or replace function public.cato_can_play_game(gid uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (
    select 1 from public.assignments a
    where a.custom_game_id = gid and public.cato_in_class(a.class_id)
  );
$$;

-- ---------- extra courses (elective bundles of lessons) ----------
create table if not exists public.extra_courses (
  id           uuid primary key default gen_random_uuid(),
  created_by   uuid references public.profiles(id) on delete set null,
  school_id    uuid references public.schools(id) on delete set null,
  title        text not null,
  description  text,
  cover_emoji  text not null default '📘',
  subject_id   text references public.subjects(id) on delete set null,
  grade        public.grade_level,
  lesson_ids   jsonb not null default '[]'::jsonb,
  is_published boolean not null default true,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);
create index if not exists extra_courses_school_idx on public.extra_courses(school_id);

-- ---------- activities (offline / creative / physical tasks) ----------
create table if not exists public.activities (
  id            uuid primary key default gen_random_uuid(),
  created_by    uuid references public.profiles(id) on delete set null,
  school_id     uuid references public.schools(id) on delete set null,
  title         text not null,
  instructions  text not null default '',
  activity_type text not null default 'creative',
  grade         public.grade_level,
  points_reward integer not null default 10,
  is_published  boolean not null default true,
  created_at    timestamptz not null default now()
);
create index if not exists activities_school_idx on public.activities(school_id);

-- ---------- widen assignments into the general "set work" table ----------
alter table public.assignments alter column lesson_id drop not null;
alter table public.assignments add column if not exists title text;
alter table public.assignments add column if not exists type text not null default 'lesson';
alter table public.assignments add column if not exists instructions text;
alter table public.assignments add column if not exists points_reward integer not null default 10;
alter table public.assignments add column if not exists requires_submission boolean not null default false;
alter table public.assignments add column if not exists custom_game_id uuid references public.custom_games(id) on delete set null;
alter table public.assignments add column if not exists course_id uuid references public.extra_courses(id) on delete set null;
alter table public.assignments add column if not exists activity_id uuid references public.activities(id) on delete set null;

do $$ begin
  alter table public.assignments
    add constraint assignments_type_check check (type in ('lesson','homework','activity','course'));
exception when duplicate_object then null; end $$;

-- ---------- what a student hands back for a homework / activity ----------
create table if not exists public.assignment_submissions (
  id               uuid primary key default gen_random_uuid(),
  assignment_id    uuid not null references public.assignments(id) on delete cascade,
  student_id       uuid not null references public.profiles(id) on delete cascade,
  answer_text      text,
  status           text not null default 'submitted',
  score            integer,
  teacher_feedback text,
  reviewed_by      uuid references public.profiles(id) on delete set null,
  reviewed_at      timestamptz,
  submitted_at     timestamptz not null default now(),
  unique (assignment_id, student_id)
);
do $$ begin
  alter table public.assignment_submissions
    add constraint assignment_submissions_status_check check (status in ('submitted','reviewed','needs_revision'));
exception when duplicate_object then null; end $$;
create index if not exists assignment_submissions_assignment_idx on public.assignment_submissions(assignment_id);
create index if not exists assignment_submissions_student_idx   on public.assignment_submissions(student_id);

-- ---------- student avatar (character creator) ----------
create table if not exists public.student_avatar (
  student_id uuid primary key references public.profiles(id) on delete cascade,
  config     jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now()
);

-- ---------- what a student owns from the coin shop ----------
create table if not exists public.student_inventory (
  id          uuid primary key default gen_random_uuid(),
  student_id  uuid not null references public.profiles(id) on delete cascade,
  item_key    text not null,
  acquired_at timestamptz not null default now(),
  unique (student_id, item_key)
);
create index if not exists student_inventory_student_idx on public.student_inventory(student_id);

-- ============================================================
-- RLS
-- ============================================================
alter table public.custom_games          enable row level security;
alter table public.extra_courses         enable row level security;
alter table public.activities            enable row level security;
alter table public.assignment_submissions enable row level security;
alter table public.student_avatar        enable row level security;
alter table public.student_inventory     enable row level security;

-- ---------- custom_games ----------
drop policy if exists custom_games_select on public.custom_games;
create policy custom_games_select on public.custom_games for select to authenticated
using (
  public.cato_is_admin()
  or created_by = auth.uid()
  or (school_id is not null and school_id = public.cato_school() and public.cato_is_creator())
  or (is_published and public.cato_my_school(school_id))
  or public.cato_can_play_game(id)
);

drop policy if exists custom_games_write on public.custom_games;
create policy custom_games_write on public.custom_games for all to authenticated
using (
  public.cato_is_admin() or created_by = auth.uid()
  or (school_id is not null and school_id = public.cato_school() and public.cato_is_creator())
)
with check (public.cato_is_admin() or public.cato_is_creator());

-- ---------- extra_courses ----------
drop policy if exists extra_courses_select on public.extra_courses;
create policy extra_courses_select on public.extra_courses for select to authenticated
using (
  public.cato_is_admin()
  or created_by = auth.uid()
  or (school_id is not null and school_id = public.cato_school() and public.cato_is_creator())
  or (is_published and public.cato_my_school(school_id))
);

drop policy if exists extra_courses_write on public.extra_courses;
create policy extra_courses_write on public.extra_courses for all to authenticated
using (
  public.cato_is_admin() or created_by = auth.uid()
  or (school_id is not null and school_id = public.cato_school() and public.cato_is_creator())
)
with check (public.cato_is_admin() or public.cato_is_creator());

-- ---------- activities ----------
drop policy if exists activities_select on public.activities;
create policy activities_select on public.activities for select to authenticated
using (
  public.cato_is_admin()
  or created_by = auth.uid()
  or (school_id is not null and school_id = public.cato_school() and public.cato_is_creator())
  or (is_published and public.cato_my_school(school_id))
);

drop policy if exists activities_write on public.activities;
create policy activities_write on public.activities for all to authenticated
using (
  public.cato_is_admin() or created_by = auth.uid()
  or (school_id is not null and school_id = public.cato_school() and public.cato_is_creator())
)
with check (public.cato_is_admin() or public.cato_is_creator());

-- ---------- assignment_submissions ----------
drop policy if exists assignment_submissions_select on public.assignment_submissions;
create policy assignment_submissions_select on public.assignment_submissions for select to authenticated
using (
  student_id = auth.uid()
  or public.cato_is_admin()
  or public.cato_is_child(student_id)
  or exists (select 1 from public.assignments a where a.id = assignment_id and public.cato_owns_class(a.class_id))
);

drop policy if exists assignment_submissions_student_write on public.assignment_submissions;
create policy assignment_submissions_student_write on public.assignment_submissions for all to authenticated
using (student_id = auth.uid())
with check (student_id = auth.uid() and status = 'submitted');

drop policy if exists assignment_submissions_teacher_write on public.assignment_submissions;
create policy assignment_submissions_teacher_write on public.assignment_submissions for all to authenticated
using (
  public.cato_is_admin()
  or exists (select 1 from public.assignments a where a.id = assignment_id and public.cato_owns_class(a.class_id))
)
with check (
  public.cato_is_admin()
  or exists (select 1 from public.assignments a where a.id = assignment_id and public.cato_owns_class(a.class_id))
);

-- ---------- student_avatar ----------
drop policy if exists student_avatar_select on public.student_avatar;
create policy student_avatar_select on public.student_avatar for select to authenticated
using (
  student_id = auth.uid() or public.cato_is_admin() or public.cato_is_child(student_id)
  or public.cato_teaches(student_id) or public.cato_same_school(student_id)
);

drop policy if exists student_avatar_write on public.student_avatar;
create policy student_avatar_write on public.student_avatar for all to authenticated
using (student_id = auth.uid() or public.cato_is_admin())
with check (student_id = auth.uid() or public.cato_is_admin());

-- ---------- student_inventory ----------
drop policy if exists student_inventory_select on public.student_inventory;
create policy student_inventory_select on public.student_inventory for select to authenticated
using (
  student_id = auth.uid() or public.cato_is_admin() or public.cato_is_child(student_id)
  or public.cato_teaches(student_id) or public.cato_same_school(student_id)
);

drop policy if exists student_inventory_write on public.student_inventory;
create policy student_inventory_write on public.student_inventory for all to authenticated
using (student_id = auth.uid() or public.cato_is_admin())
with check (student_id = auth.uid() or public.cato_is_admin());

-- ---------- grants (new tables aren't covered by the earlier blanket grant) ----------
grant select, insert, update, delete on
  public.custom_games, public.extra_courses, public.activities,
  public.assignment_submissions, public.student_avatar, public.student_inventory
  to authenticated;

revoke all on function public.cato_is_creator()          from public, anon;
revoke all on function public.cato_my_school(uuid)        from public, anon;
revoke all on function public.cato_can_play_game(uuid)     from public, anon;
grant execute on function public.cato_is_creator()         to authenticated;
grant execute on function public.cato_my_school(uuid)       to authenticated;
grant execute on function public.cato_can_play_game(uuid)   to authenticated;
