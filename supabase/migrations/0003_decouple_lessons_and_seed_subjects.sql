-- The curriculum ships inside the app so a child can play with no network.
-- Progress rows therefore reference lesson ids as plain text: a device that is
-- ahead of the server's lesson catalogue must never fail to save a child's stars.
alter table public.lesson_progress drop constraint if exists lesson_progress_lesson_id_fkey;
alter table public.quiz_attempts   drop constraint if exists quiz_attempts_lesson_id_fkey;
alter table public.assignments     drop constraint if exists assignments_lesson_id_fkey;

create index if not exists lesson_progress_lesson_idx on public.lesson_progress(lesson_id);

insert into public.subjects (id, title, book_title, description, color, icon, sort_order) values
  ('letter_land',   'Letter Land',   'Reading, Writing & Activities', 'Letters, sounds and first words', '#F7906F', '🔤', 0),
  ('number_land',   'Number Land',   'Concepts and Number Fun',       'Counting, shapes and patterns',   '#73DBD5', '🔢', 1),
  ('know_my_world', 'Know My World', 'Fun Activities',                'The world around me',             '#91B0FF', '🌍', 2)
on conflict (id) do update
  set title = excluded.title,
      book_title = excluded.book_title,
      description = excluded.description,
      color = excluded.color,
      icon = excluded.icon,
      sort_order = excluded.sort_order;
