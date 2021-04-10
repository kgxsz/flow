(ns flow.email
  (:require [ses-mailer.core :as mailer]
            [flow.utils :as u]))


(defn send-email!
  "Sends and email with the body and subject
   provided to the email address provided."
  [email-address subject {:keys [html text]}]
  (mailer/send-email
   {}
   "Flow <noreply@flow.keigo.io>"
   (u/validate :email/email-address email-address)
   (u/validate :email/subject subject)
   {:html-body (u/validate :email/html html)
    :text-body (u/validate :email/text text)}))
