# Flow

#### A repository for messing around with Shadow CLJS and Terraform.

## Project tracking
- Create an issue and tie it to a project.
- Create a backing branch.
- Raise a draft PR, and tie it to the issue.
- The project will now be tracking the progress.


## Local development setup

### Self signed certificate setup for api
- Create and trust a self signed root SSL server certificate with:
  - Name as `api.localhost`.
  - Common name as `api.localhost`.
  - URI as `api.localhost`.
  - dNS name as `api.localhost`.
  - Empty iPAddress.
  - Choose a password, this is your keystore password.
- Export the certificate to `api/ssl/Certificates.p12`
- In `api/ssl/` generate the `keystore.jks` with: 
  `keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore Certificates.p12`.
- Ensure that the destination and source keystore passwords are equal to the keystore password above.

### Self signed certificate setup for app
- Create and trust a self signed root SSL server certificate with:
  - Name as `localhost`.
  - Common name as `localhost`.
  - URI as `localhost`.
  - dNS name as `localhost`.
  - Empty iPAddress.
  - Choose a password, this is your keystore password.
- Export the certificate to `app/ssl/Certificates.p12`
- In `app/ssl/` generate the `keystore.jks` with: 
  `keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore Certificates.p12`.
- Ensure that the destination and source keystore passwords are equal to the keystore password above.

### Local api development setup
- In `api/` setup the environment variables:
  - `KEYSTORE_PASSWORD` as determined above.
  - `CORS_ORIGIN` as `https://localhost:8080`.
  - `COOKIE_STORE_KEY` as 16 byte secret key.
- In `api/` start the REPL with `clj -A:repl`.
- Connect to the api's Clojure REPL, load `flow.dev`, and run `(server)`.
- The api will be running at `https://api.localhost:443`.

### Local app development setup
- In `app/` setup the environment variables:
  - `KEYSTORE_PASSWORD` as determined above.
- In `app/` start the auto JS compilation with `clj -A:dev/js`.
- In `app/` start the auto CSS compilation with `clj -A:dev/css`.
- Connect to the app's Clojure REPL, and start the Clojurescript REPL with `(repl)`.
- The app will be running at `https://localhost:8080`.


## Remote deployment

### Create the assets
- In `app/` build an optimised index.js file with `clj -A:release/js`.
- In `app/` build an optimised index.css file with `clj -A:release/css`.
- In `api/` clear out previous assets with `rm -rf classes && mkdir classes && rm-rf target && mkdir target`.
- In `api/` compile the API with `clj -A:compile`.
- In `api/` zip the API with `clj -A:zip mach.pack.alpha.aws-lambda target/flow.zip -C:compile -R:compile`

### Deploy the infrastructure and assets
- In `infrastructure/` setup the environment variables:
  - `TF_VAR_cookie_store_key` as the 16 byte secret key set in production.
- In `infrastructure/` update the remote assets with `terraform apply`.

### Remote tear down
- In `infrastructure/`, tear down remote assets with `terraform destroy`.
