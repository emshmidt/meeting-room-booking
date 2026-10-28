ALTER TABLE public.rooms
ADD CHECK (capacity > 0);

CREATE TABLE public.users (
                              id uuid PRIMARY KEY,
                              name text NOT NULL,
                              email text NOT NULL UNIQUE,
                              role text NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE public.time_slots (
                              id uuid PRIMARY KEY,
                              room_id uuid NOT NULL REFERENCES public.rooms(id) ON DELETE RESTRICT,
                              starts_at timestamptz NOT NULL,
                              ends_at timestamptz NOT NULL,
                              CHECK (ends_at - starts_at = INTERVAL '1 hour'),
                              CHECK (starts_at = date_trunc('hour', starts_at, 'UTC')),
                              UNIQUE (room_id, starts_at)
);

CREATE TABLE public.bookings (
                              id uuid PRIMARY KEY,
                              user_id uuid NOT NULL REFERENCES public.users(id) ON DELETE RESTRICT,
                              slot_id uuid NOT NULL REFERENCES public.time_slots(id) ON DELETE RESTRICT,
                              status text NOT NULL CHECK ( status IN ('ACTIVE', 'CANCELLED') ) DEFAULT 'ACTIVE',
                              created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);