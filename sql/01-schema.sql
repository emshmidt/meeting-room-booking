CREATE TABLE public.rooms (
                              id uuid PRIMARY KEY,
                              name text NOT NULL,
                              capacity integer NOT NULL,
                              has_projector boolean NOT NULL,
                              description text
);