insert into public.channels (name, description)
values ('newzle', 'The Daily Newzle Channel'),
       ('draft', 'Draft Channel')
on conflict do nothing;