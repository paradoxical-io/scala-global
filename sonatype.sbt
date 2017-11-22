import com.typesafe.sbt.pgp.PgpKeys.{pgpPassphrase, pgpPublicRing, pgpSecretRing}
import xerial.sbt.Sonatype.SonatypeKeys.sonatypeProfileName

credentials += Credentials("Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USER", default = "paradoxicalio"),
  sys.env.getOrElse("SONATYPE_PASSWORD", default = "")),

pgpPublicRing := new File(".deployment/gpg/paradoxical-io.pubgpg"),
pgpSecretRing := new File(".deployment/gpg/paradoxical-io-private.gpg"),
pgpPassphrase := Some(sys.env.getOrElse("GPG_PASSWORD", default = "").toArray),

sonatypeProfileName := "io.paradoxical",
