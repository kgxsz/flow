# Flow

#### A repository for messing around with Shadow CLJS and Terraform.

## Project tracking
- Create an issue and tie it to a project.
- Create a backing branch.
- Raise a draft PR, and tie it to the issue.
- The project will now be tracking the progress.

## Local development setup
- In `app/` start the auto JS compilation with `clj A:dev/js`.
- In `app/` start the auto CSS compilation with `clj A:dev/css`.
- In `api/` start the REPL with `clj A:repl`.
- Connect to the app's Clojure REPL, and start the Clojurescript REPL with `(repl)`.
- Connect to the api's Clojure REPL, and load the `flow.dev` namespace to start the server.
- Go to `localhost:8080` in the browser to see the app.

## Remote Deployment
- In `app/` build an optimised index.js file with `clj A:release/js`.
- In `app/` build an optimised index.css file with `clj A:release/css`.
- In `infrastructure/` update the remote assets with `terraform apply`.

## Tear down
- In `infrastructure/`, tear down remote assets with `terraform destroy`.
