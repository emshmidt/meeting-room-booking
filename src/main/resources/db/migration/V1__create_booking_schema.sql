CREATE TABLE public.rooms (
                              id uuid PRIMARY KEY,
                              name text NOT NULL,
                              capacity integer NOT NULL CONSTRAINT rooms_capacity_positive CHECK (capacity > 0),
                              has_projector boolean NOT NULL,
                              description text
);

CREATE TABLE public.users (
                              id uuid PRIMARY KEY,
                              name text NOT NULL,
                              email text NOT NULL CONSTRAINT users_email_key UNIQUE,
                              role text NOT NULL CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE public.time_slots (
                              id uuid PRIMARY KEY,
                              room_id uuid NOT NULL CONSTRAINT time_slots_room_fk
                                  REFERENCES public.rooms(id) ON DELETE RESTRICT,
                              starts_at timestamptz NOT NULL,
                              ends_at timestamptz NOT NULL,
                              CONSTRAINT time_slots_duration_one_hour CHECK (ends_at - starts_at = INTERVAL '1 hour'),
                              CONSTRAINT time_slots_start_on_utc_hour CHECK (starts_at = date_trunc('hour', starts_at, 'UTC')),
                              CONSTRAINT time_slots_room_start_key UNIQUE (room_id, starts_at)
);

CREATE TABLE public.bookings (
                              id uuid PRIMARY KEY,
                              user_id uuid NOT NULL CONSTRAINT bookings_user_fk REFERENCES public.users(id) ON DELETE RESTRICT,
                              slot_id uuid NOT NULL CONSTRAINT bookings_time_slot_fk REFERENCES public.time_slots(id) ON DELETE RESTRICT,
                              status text NOT NULL CONSTRAINT bookings_status_check CHECK ( status IN ('ACTIVE', 'CANCELLED') ) DEFAULT 'ACTIVE',
                              created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX bookings_one_active_per_slot_idx
    ON public.bookings (slot_id) WHERE status = 'ACTIVE';