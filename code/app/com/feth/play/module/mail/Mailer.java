package com.feth.play.module.mail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import akka.actor.Cancellable;
import play.Configuration;
import play.libs.mailer.Email;

public interface Mailer {

	public static class Configs {
		static final String MAILER = "play-easymail/";
		static final String CONFIG_BASE = "play-easymail";
	}

	public class SettingKeys {
		public static final String FROM = "from";
		public static final String FROM_EMAIL = "email";
		public static final String FROM_NAME = "name";
		public static final String INCLUDE_XMAILER_HEADER = "includeXMailerHeader";
		public static final String DELAY = "delay";
		public static final String VERSION = "version";
	}

	public static String getEmailName(final String email, final String name) {
		if (email == null || email.trim().isEmpty()) {
			throw new RuntimeException("email must not be null");
		}
		final StringBuilder sb = new StringBuilder();
		final boolean hasName = name != null && !name.trim().isEmpty();
		if (hasName) {
			sb.append("\"");
			sb.append(name);
			sb.append("\" <");
		}

		sb.append(email);

		if (hasName) {
			sb.append(">");
		}

		return sb.toString();
	}

	public static class Mail extends Email {

		public static class HtmlBody extends Body {

			public HtmlBody(final String text) {
				super(null, text);
			}

		}

		public static class TxtBody extends Body {

			public TxtBody(final String text) {
				super(text, null);
			}

		}

		public static class Body {
			private final String html;
			private final String text;
			private final boolean isHtml;
			private final boolean isText;

			public Body(final String text) {
				this(text, null);
			}

			public Body(final String text, final String html) {
				this.isHtml = html != null && !html.trim().isEmpty();
				this.isText = text != null && !text.trim().isEmpty();

				if (!this.isHtml && !this.isText) {
					throw new RuntimeException("Text and HTML cannot both be empty or null");
				}
				this.html = (this.isHtml) ? html : null;
				this.text = (this.isText) ? text : null;
			}

			public boolean isHtml() {
				return this.isHtml;
			}

			public boolean isText() {
				return this.isText;
			}

			public boolean isBoth() {
				return isText() && isHtml();
			}

			public String getHtml() {
				return this.html;
			}

			public String getText() {
				return this.text;
			}
		}

		public Mail(final String subject, final String body, final String recipient) {
			this(subject, new Body(body), new String[] { recipient });
		}

		public Mail(final String subject, final String body, final List<String> recipient) {
			this(subject, new Body(body), recipient);
		}

		public Mail(final String subject, final Body body, final String recipient) {
			this(subject, body, new String[] { recipient });
		}

		public Mail(final String subject, final Body body, final String[] recipients) {
			this(subject, body, Arrays.asList(recipients));
		}

		public Mail(final String subject, final Body body, final List<String> recipients) {
			this(subject, body, recipients, null, null, null, null);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc) {
			this(subject, body, recipients, cc, null, null, null);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc,
				final List<String> bcc) {
			this(subject, body, recipients, cc, bcc, null, null);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final String replyTo) {
			this(subject, body, recipients, null, null, null, replyTo);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc,
				final String replyTo) {
			this(subject, body, recipients, cc, null, null, replyTo);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc,
				final List<String> bcc, final String replyTo) {
			this(subject, body, recipients, cc, bcc, null, replyTo);
		}

		public Mail(final String subject, final Body body, final List<String> recipients,
				final Map<String, List<String>> customHeaders) {
			this(subject, body, recipients, null, null, customHeaders, null);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc,
				final Map<String, List<String>> customHeaders) {
			this(subject, body, recipients, cc, null, customHeaders, null);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc,
				final List<String> bcc, final Map<String, List<String>> customHeaders) {
			this(subject, body, recipients, cc, bcc, customHeaders, null);
		}

		public Mail(final String subject, final Body body, final List<String> recipients, final List<String> cc,
				final List<String> bcc, final Map<String, List<String>> customHeaders, final String replyTo) {
			if (subject == null || subject.trim().isEmpty()) {
				throw new RuntimeException("Subject must not be null or empty");
			}
			this.setSubject(subject);

			if (body == null) {
				throw new RuntimeException("Body must not be null or empty");
			}
			if (body.isText())
				this.setBodyText(body.getText());
			if (body.isHtml())
				this.setBodyHtml(body.getHtml());

			if (recipients == null || recipients.size() == 0) {
				throw new RuntimeException("There must be at least one recipient");
			}
			this.setTo(recipients);

			if (cc != null && cc.size() > 0)
				this.setCc(cc);
			if (bcc != null && bcc.size() > 0)
				this.setBcc(bcc);

			if (customHeaders != null) {
				for (final Entry<String, List<String>> entry : customHeaders.entrySet()) {
					final String headerName = entry.getKey();
					for (final String headerValue : entry.getValue()) {
						this.addHeader(headerName, headerValue);
					}
				}
			}
			this.setReplyTo(replyTo);
		}

		public Body getBody() {
			return new Body(getBodyText(), getBodyHtml());
		}
	}

	public Cancellable sendMail(final Mail email);

	public Cancellable sendMail(final String subject, final String textBody, final String recipient);

	public Cancellable sendMail(final String subject, final Mail.Body body, final String recipient);

	public interface MailerFactory {
		public MailerImpl create(final Configuration mailerConfig);
	}

}
