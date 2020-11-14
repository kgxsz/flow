# Flow

#### A repository for messing around with Shadow CLJS and Terraform.

## Project tracking
- Create an issue and tie it to a project.
- Create a backing branch.
- Raise a draft PR, and tie it to the issue.
- The project will now be tracking the progress.

## Local development setup
- Start the auto JS compilation with `clj A:dev/js`.
- Start the auto CSS compilation with `clj A:dev/css`.
- Connect to the Clojure REPL, and start the Clojurescript REPL with `(repl)`.
- Go to `localhost:8080` in the browser to see the app.

## Remote Deployment
- In `app/` build an optimised index.js file with `shadow-cljs release :app`.
- In `infrastructure/` update the remote assets with `terraform apply`.

## Tear down
- In `infrastructure/`, tear down remote assets with `terraform destroy`.
