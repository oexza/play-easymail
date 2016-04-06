package com.feth.play.module.mail;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import play.Configuration;
import play.libs.mailer.MailerClient;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class MailerImpl implements Mailer {

	protected final FiniteDuration delay;

	protected final String sender;

	protected final boolean includeXMailerHeader;

	protected final Configuration configuration;

	protected final MailerClient mailClient;

	protected final ActorSystem actorSystem;

	private String getVersion() {
		return getConfiguration().getString(SettingKeys.VERSION);
	}

	private Configuration getConfiguration() {
		return configuration.getConfig(Configs.CONFIG_BASE);
	}

	@Inject
	public MailerImpl(final Configuration configuration, @Assisted final Configuration mailerConfig,
			final MailerClient mailClient, final ActorSystem actorSystem) {
		this.configuration = configuration;
		this.mailClient = mailClient;
		this.actorSystem = actorSystem;

		this.delay = Duration.create(mailerConfig.getLong(SettingKeys.DELAY, 1L), TimeUnit.SECONDS);

		final Configuration fromConfig = mailerConfig.getConfig(SettingKeys.FROM);
		this.sender = Mailer.getEmailName(fromConfig.getString(SettingKeys.FROM_EMAIL),
				fromConfig.getString(SettingKeys.FROM_NAME));

		this.includeXMailerHeader = mailerConfig.getBoolean(SettingKeys.INCLUDE_XMAILER_HEADER, true);
	}

	private class MailJob implements Runnable {

		private Mail mail;

		public MailJob(final Mail m) {
			this.mail = m;
		}

		@Override
		public void run() {
			if (MailerImpl.this.includeXMailerHeader) {
				this.mail.addHeader("X-Mailer", Configs.MAILER + getVersion());
			}
			mailClient.send(this.mail);
		}

	}

	@Override
	public Cancellable sendMail(final Mail email) {
		email.setFrom(this.sender);
		return actorSystem.scheduler().scheduleOnce(this.delay, new MailJob(email), actorSystem.dispatcher());
	}

	@Override
	public Cancellable sendMail(final String subject, final String textBody, final String recipient) {
		final Mail mail = new Mail(subject, new Mail.Body(textBody), Arrays.asList(recipient));
		return sendMail(mail);
	}

	@Override
	public Cancellable sendMail(final String subject, final Mail.Body body, final String recipient) {
		final Mail mail = new Mail(subject, body, Arrays.asList(recipient));
		return sendMail(mail);
	}
}
