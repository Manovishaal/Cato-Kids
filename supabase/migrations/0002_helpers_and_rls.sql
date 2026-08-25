-- ============================================================
-- Cato Kids — helper functions, signup trigger, Row Level Security
-- ============================================================

-- ---------- helper functions (security definer, avoid RLS recursion) ----------
create or replace function public.cato_role()
returns public.user_role
language sql stable security definer set search_path = public, pg_temp as $$
  select role from public.profiles where id = auth.uid();
$$;

create or replace function public.cato_school()
returns uuid
language sql stable security definer set search_path = public, pg_temp as $$
  select school_id from public.profiles where id = auth.uid();
$$;

create or replace function public.cato_is_admin()
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select coalesce((select role from public.profiles where id = auth.uid()) = 'admin', false);
$$;

create or replace function public.cato_is_school_staff()
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select coalesce((select role from public.profiles where id = auth.uid()) in ('admin','school'), false);
$$;

create or replace function public.cato_is_child(child uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (select 1 from public.parent_children pc
                 where pc.parent_id = auth.uid() and pc.student_id = child);
$$;

create or replace function public.cato_teaches(student uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (select 1 from public.class_students cs
                 join public.classes c on c.id = cs.class_id
                 where cs.student_id = student and c.teacher_id = auth.uid());
$$;

create or replace function public.cato_same_school(target uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (
    select 1 from public.profiles me, public.profiles them
    where me.id = auth.uid() and them.id = target
      and me.school_id is not null and me.school_id = them.school_id
      and me.role in ('school','admin','teacher'));
$$;

create or replace function public.cato_in_class(cls uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (select 1 from public.class_students cs where cs.class_id = cls and cs.student_id = auth.uid())
      or exists (select 1 from public.class_students cs
                 join public.parent_children pc on pc.student_id = cs.student_id
                 where cs.class_id = cls and pc.parent_id = auth.uid());
$$;

create or replace function public.cato_owns_class(cls uuid)
returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (
    select 1 from public.classes c
    where c.id = cls
      and (c.teacher_id = auth.uid()
           or (c.school_id is not null and c.school_id = (select school_id from public.profiles where id = auth.uid())
               and (select role from public.profiles where id = auth.uid()) in ('school','admin'))));
$$;

-- ---------- auto profile on signup ----------
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public, pg_temp as $$
declare
  v_role  public.user_role := 'student';
  v_grade public.grade_level;
begin
  begin
    v_role := coalesce(nullif(new.raw_user_meta_data->>'role','')::public.user_role, 'student');
  exception when others then v_role := 'student';
  end;
  begin
    v_grade := nullif(new.raw_user_meta_data->>'grade','')::public.grade_level;
  exception when others then v_grade := null;
  end;

  insert into public.profiles (id, role, full_name, email, grade, phone)
  values (new.id, v_role,
          coalesce(nullif(new.raw_user_meta_data->>'full_name',''), split_part(coalesce(new.email,'friend'),'@',1)),
          new.email, v_grade, nullif(new.raw_user_meta_data->>'phone',''))
  on conflict (id) do nothing;
  return new;
end $$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

create or replace function public.touch_updated_at()
returns trigger language plpgsql as $$
begin new.updated_at := now(); return new; end $$;

drop trigger if exists profiles_touch on public.profiles;
create trigger profiles_touch before update on public.profiles
  for each row execute function public.touch_updated_at();

-- ---------- enable RLS ----------
alter table public.schools         enable row level security;
alter table public.profiles        enable row level security;
alter table public.classes         enable row level security;
alter table public.class_students  enable row level security;
alter table public.parent_children enable row level security;
alter table public.subjects        enable row level security;
alter table public.lessons         enable row level security;
alter table public.lesson_progress enable row level security;
alter table public.quiz_attempts   enable row level security;
alter table public.assignments     enable row level security;
alter table public.announcements   enable row level security;

-- ---------- profiles ----------
drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles for select to authenticated
using (id = auth.uid() or public.cato_is_admin() or public.cato_is_child(id)
       or public.cato_teaches(id) or public.cato_same_school(id));

drop policy if exists profiles_insert on public.profiles;
create policy profiles_insert on public.profiles for insert to authenticated
with check (id = auth.uid() or public.cato_is_school_staff());

drop policy if exists profiles_update on public.profiles;
create policy profiles_update on public.profiles for update to authenticated
using (id = auth.uid() or public.cato_is_admin() or public.cato_same_school(id))
with check (id = auth.uid() or public.cato_is_admin() or public.cato_same_school(id));

drop policy if exists profiles_delete on public.profiles;
create policy profiles_delete on public.profiles for delete to authenticated
using (public.cato_is_admin());

-- ---------- schools ----------
drop policy if exists schools_select on public.schools;
create policy schools_select on public.schools for select to authenticated using (true);

drop policy if exists schools_write on public.schools;
create policy schools_write on public.schools for all to authenticated
using (public.cato_is_admin() or id = public.cato_school())
with check (public.cato_is_admin() or id = public.cato_school());

-- ---------- classes ----------
drop policy if exists classes_select on public.classes;
create policy classes_select on public.classes for select to authenticated
using (public.cato_is_admin() or teacher_id = auth.uid()
       or (school_id is not null and school_id = public.cato_school())
       or public.cato_in_class(id));

drop policy if exists classes_write on public.classes;
create policy classes_write on public.classes for all to authenticated
using (public.cato_is_admin() or teacher_id = auth.uid()
       or (school_id is not null and school_id = public.cato_school() and public.cato_is_school_staff()))
with check (public.cato_is_admin() or teacher_id = auth.uid()
       or (school_id is not null and school_id = public.cato_school() and public.cato_is_school_staff()));

-- ---------- class_students ----------
drop policy if exists class_students_select on public.class_students;
create policy class_students_select on public.class_students for select to authenticated
using (student_id = auth.uid() or public.cato_is_admin() or public.cato_is_child(student_id)
       or public.cato_owns_class(class_id));

drop policy if exists class_students_write on public.class_students;
create policy class_students_write on public.class_students for all to authenticated
using (public.cato_is_admin() or public.cato_owns_class(class_id))
with check (public.cato_is_admin() or public.cato_owns_class(class_id));

-- ---------- parent_children ----------
drop policy if exists parent_children_select on public.parent_children;
create policy parent_children_select on public.parent_children for select to authenticated
using (parent_id = auth.uid() or student_id = auth.uid() or public.cato_is_admin()
       or public.cato_teaches(student_id) or public.cato_same_school(student_id));

drop policy if exists parent_children_write on public.parent_children;
create policy parent_children_write on public.parent_children for all to authenticated
using (parent_id = auth.uid() or public.cato_is_school_staff())
with check (parent_id = auth.uid() or public.cato_is_school_staff());

-- ---------- curriculum (read-all, admin-write) ----------
drop policy if exists subjects_select on public.subjects;
create policy subjects_select on public.subjects for select to authenticated using (true);
drop policy if exists subjects_write on public.subjects;
create policy subjects_write on public.subjects for all to authenticated
using (public.cato_is_admin()) with check (public.cato_is_admin());

drop policy if exists lessons_select on public.lessons;
create policy lessons_select on public.lessons for select to authenticated using (is_published or public.cato_is_admin());
drop policy if exists lessons_write on public.lessons;
create policy lessons_write on public.lessons for all to authenticated
using (public.cato_is_admin()) with check (public.cato_is_admin());

-- ---------- lesson_progress ----------
drop policy if exists lesson_progress_select on public.lesson_progress;
create policy lesson_progress_select on public.lesson_progress for select to authenticated
using (student_id = auth.uid() or public.cato_is_admin() or public.cato_is_child(student_id)
       or public.cato_teaches(student_id) or public.cato_same_school(student_id));

drop policy if exists lesson_progress_write on public.lesson_progress;
create policy lesson_progress_write on public.lesson_progress for all to authenticated
using (student_id = auth.uid() or public.cato_is_admin())
with check (student_id = auth.uid() or public.cato_is_admin());

-- ---------- quiz_attempts ----------
drop policy if exists quiz_attempts_select on public.quiz_attempts;
create policy quiz_attempts_select on public.quiz_attempts for select to authenticated
using (student_id = auth.uid() or public.cato_is_admin() or public.cato_is_child(student_id)
       or public.cato_teaches(student_id) or public.cato_same_school(student_id));

drop policy if exists quiz_attempts_write on public.quiz_attempts;
create policy quiz_attempts_write on public.quiz_attempts for all to authenticated
using (student_id = auth.uid() or public.cato_is_admin())
with check (student_id = auth.uid() or public.cato_is_admin());

-- ---------- assignments ----------
drop policy if exists assignments_select on public.assignments;
create policy assignments_select on public.assignments for select to authenticated
using (public.cato_is_admin() or public.cato_owns_class(class_id) or public.cato_in_class(class_id));

drop policy if exists assignments_write on public.assignments;
create policy assignments_write on public.assignments for all to authenticated
using (public.cato_is_admin() or public.cato_owns_class(class_id))
with check (public.cato_is_admin() or public.cato_owns_class(class_id));

-- ---------- announcements ----------
drop policy if exists announcements_select on public.announcements;
create policy announcements_select on public.announcements for select to authenticated
using (public.cato_is_admin()
       or (school_id is not null and school_id = public.cato_school())
       or (class_id is not null and (public.cato_in_class(class_id) or public.cato_owns_class(class_id)))
       or (school_id is null and class_id is null));

drop policy if exists announcements_write on public.announcements;
create policy announcements_write on public.announcements for all to authenticated
using (public.cato_is_admin() or author_id = auth.uid()
       or (school_id is not null and school_id = public.cato_school() and public.cato_is_school_staff()))
with check (public.cato_is_admin() or author_id = auth.uid()
       or (school_id is not null and school_id = public.cato_school() and public.cato_is_school_staff()));

grant usage on schema public to anon, authenticated;
grant select, insert, update, delete on all tables in schema public to authenticated;
grant select on public.subjects, public.lessons, public.schools to anon;
