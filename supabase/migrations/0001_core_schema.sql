-- ============================================================
-- Cato Kids — core schema
-- ============================================================

create extension if not exists "pgcrypto";

do $$ begin
  create type public.user_role as enum ('student','teacher','parent','admin','school');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.grade_level as enum ('PREKG','LKG','UKG');
exception when duplicate_object then null; end $$;

-- ---------- schools ----------
create table if not exists public.schools (
  id          uuid primary key default gen_random_uuid(),
  name        text not null,
  code        text not null unique,
  address     text,
  city        text,
  phone       text,
  email       text,
  logo_url    text,
  created_at  timestamptz not null default now()
);

-- ---------- profiles ----------
create table if not exists public.profiles (
  id            uuid primary key references auth.users(id) on delete cascade,
  role          public.user_role not null default 'student',
  full_name     text not null default '',
  email         text,
  phone         text,
  avatar_url    text,
  school_id     uuid references public.schools(id) on delete set null,
  grade         public.grade_level,
  date_of_birth date,
  coins         integer not null default 0,
  stars         integer not null default 0,
  streak_days   integer not null default 0,
  last_active   timestamptz,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);
create index if not exists profiles_school_idx on public.profiles(school_id);
create index if not exists profiles_role_idx   on public.profiles(role);

-- ---------- classes ----------
create table if not exists public.classes (
  id            uuid primary key default gen_random_uuid(),
  school_id     uuid references public.schools(id) on delete cascade,
  teacher_id    uuid references public.profiles(id) on delete set null,
  name          text not null,
  grade         public.grade_level not null,
  academic_year text default to_char(now(),'YYYY'),
  created_at    timestamptz not null default now()
);
create index if not exists classes_school_idx  on public.classes(school_id);
create index if not exists classes_teacher_idx on public.classes(teacher_id);

create table if not exists public.class_students (
  class_id   uuid not null references public.classes(id)  on delete cascade,
  student_id uuid not null references public.profiles(id) on delete cascade,
  joined_at  timestamptz not null default now(),
  primary key (class_id, student_id)
);
create index if not exists class_students_student_idx on public.class_students(student_id);

create table if not exists public.parent_children (
  parent_id    uuid not null references public.profiles(id) on delete cascade,
  student_id   uuid not null references public.profiles(id) on delete cascade,
  relationship text default 'parent',
  primary key (parent_id, student_id)
);
create index if not exists parent_children_student_idx on public.parent_children(student_id);

-- ---------- curriculum ----------
create table if not exists public.subjects (
  id          text primary key,
  title       text not null,
  book_title  text,
  description text,
  color       text,
  icon        text,
  sort_order  integer not null default 0
);

create table if not exists public.lessons (
  id           text primary key,
  subject_id   text not null references public.subjects(id) on delete cascade,
  grade        public.grade_level not null,
  title        text not null,
  subtitle     text,
  description  text,
  sort_order   integer not null default 0,
  game_type    text not null,
  content      jsonb not null default '{}'::jsonb,
  is_published boolean not null default true,
  created_at   timestamptz not null default now()
);
create index if not exists lessons_grade_subject_idx on public.lessons(grade, subject_id, sort_order);

-- ---------- progress ----------
create table if not exists public.lesson_progress (
  id             uuid primary key default gen_random_uuid(),
  student_id     uuid not null references public.profiles(id) on delete cascade,
  lesson_id      text not null references public.lessons(id)  on delete cascade,
  stars          integer not null default 0,
  best_score     integer not null default 0,
  attempts       integer not null default 0,
  seconds_spent  integer not null default 0,
  completed      boolean not null default false,
  last_played_at timestamptz not null default now(),
  unique (student_id, lesson_id)
);
create index if not exists lesson_progress_student_idx on public.lesson_progress(student_id);

create table if not exists public.quiz_attempts (
  id               uuid primary key default gen_random_uuid(),
  student_id       uuid not null references public.profiles(id) on delete cascade,
  lesson_id        text references public.lessons(id) on delete set null,
  score            integer not null default 0,
  total_questions  integer not null default 0,
  correct_count    integer not null default 0,
  wrong_count      integer not null default 0,
  duration_seconds integer not null default 0,
  detail           jsonb not null default '{}'::jsonb,
  created_at       timestamptz not null default now()
);
create index if not exists quiz_attempts_student_idx on public.quiz_attempts(student_id, created_at desc);

create table if not exists public.assignments (
  id          uuid primary key default gen_random_uuid(),
  class_id    uuid not null references public.classes(id) on delete cascade,
  lesson_id   text not null references public.lessons(id) on delete cascade,
  assigned_by uuid references public.profiles(id) on delete set null,
  due_date    date,
  note        text,
  created_at  timestamptz not null default now()
);
create index if not exists assignments_class_idx on public.assignments(class_id);

create table if not exists public.announcements (
  id         uuid primary key default gen_random_uuid(),
  school_id  uuid references public.schools(id) on delete cascade,
  class_id   uuid references public.classes(id) on delete cascade,
  author_id  uuid references public.profiles(id) on delete set null,
  title      text not null,
  body       text not null,
  audience   public.user_role,
  created_at timestamptz not null default now()
);
create index if not exists announcements_school_idx on public.announcements(school_id, created_at desc);
