(ns flow.email
  (:require [ses-mailer.core :as mailer]
            [flow.utils :as u]
            [slingshot.slingshot :as slingshot]))


(defn send-email!
  "Sends and email with the body and subject
   provided to the email address provided."
  [email-address {:keys [subject body]}]
  (slingshot/try+
   (mailer/send-email
    {}
    "Flow <noreply@flow.keigo.io>"
    (u/validate :email/email-address email-address)
    (u/validate :email/subject subject)
    {:html-body (u/validate :email/html (:html body))
     :text-body (u/validate :email/text (:text body))})
   (catch [:type :flow/internal-error] _ (slingshot/throw+))
   (catch Object _
     (u/generate :external-error "Unable to send an email with SES."))))
