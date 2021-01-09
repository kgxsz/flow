# Flow

#### A repository for messing around with Shadow CLJS and Terraform.

## Project tracking
- Create an issue and tie it to a project.
- Create a backing branch.
- Raise a draft PR, and tie it to the issue.
- The project will now be tracking the progress.

## Local api development setup
- In `api/` setup the environment variables:
  - `CORS_ORIGIN` as `http://localhost:8080`.
  - `COOKIE_STORE_KEY` as 16 byte secret key.
  - `COOKIE_ATTRIBUTE_SECURE` as `false`.
- In `api/` start the REPL with `clj -A:repl`.
- Connect to the api's Clojure REPL, load `flow.dev`, and run `(server)`.
- The api will be running at `api.localhost:80`.

## Local app development setup
- In `app/` start the auto JS compilation with `clj -A:dev/js`.
- In `app/` start the auto CSS compilation with `clj -A:dev/css`.
- Connect to the app's Clojure REPL, and start the Clojurescript REPL with `(repl)`.
- The app will be running at `localhost:8080`.

## Remote deployment
- In `app/` build an optimised index.js file with `clj -A:release/js`.
- In `app/` build an optimised index.css file with `clj -A:release/css`.
- In `api/` clear out previous assets with `rm -rf classes && mkdir classes && rm-rf target && mkdir target`.
- In `api/` compile the API with `clj -A:compile`.
- In `api/` zip the API with `clj -A:zip mach.pack.alpha.aws-lambda target/flow.zip -C:compile -R:compile`
- In `infrastructure/` setup the environment variables:
  - `TF_VAR_cookie_store_key` as a 16 byte secret key.
- In `infrastructure/` update the remote assets with `terraform apply`.

## Tear down
- In `infrastructure/`, tear down remote assets with `terraform destroy`.
