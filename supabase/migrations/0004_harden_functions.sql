-- Pin the search_path on the one function that was missing it.
alter function public.touch_updated_at() set search_path = public, pg_temp;

-- The RLS helper functions must stay callable by `authenticated`, because policy
-- expressions are evaluated as the querying role. Nobody else needs them, so drop
-- the implicit PUBLIC/anon grants that expose them over /rest/v1/rpc.
do $$
declare fn text;
begin
  foreach fn in array array[
    'public.cato_role()',
    'public.cato_school()',
    'public.cato_is_admin()',
    'public.cato_is_school_staff()',
    'public.cato_is_child(uuid)',
    'public.cato_teaches(uuid)',
    'public.cato_same_school(uuid)',
    'public.cato_in_class(uuid)',
    'public.cato_owns_class(uuid)'
  ] loop
    execute format('revoke all on function %s from public, anon', fn);
    execute format('grant execute on function %s to authenticated', fn);
  end loop;
end $$;

-- Trigger functions are never called directly.
revoke all on function public.handle_new_user()  from public, anon, authenticated;
revoke all on function public.touch_updated_at() from public, anon, authenticated;
