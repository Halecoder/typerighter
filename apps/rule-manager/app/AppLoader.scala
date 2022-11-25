import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider => AwsCredentialsProviderV2, DefaultCredentialsProvider => DefaultCredentialsProviderV2, ProfileCredentialsProvider => ProfileCredentialsProviderV2}
import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import play.api.ApplicationLoader.Context
import play.api._
import com.gu.AppIdentity
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.gu.DevIdentity
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.gu.AwsIdentity
import com.gu.conf.SSMConfigurationLocation
import com.gu.conf.ConfigurationLoader
import play.api.Mode.Dev


class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }

    val region = context.initialConfiguration.getOptional[String]("aws.region").getOrElse("eu-west-1")

    val creds: AWSCredentialsProvider = context.environment.mode match {
      case Dev => new ProfileCredentialsProvider("composer")
      case _ => DefaultAWSCredentialsProviderChain.getInstance
    }

    val credsV2: AwsCredentialsProviderV2 = context.environment.mode match {
      case Dev => ProfileCredentialsProviderV2.create("composer")
      case _ => DefaultCredentialsProviderV2.create()
    }

    val identity = AppIdentity.whoAmI(defaultAppName = "typerighter-rule-manager", credsV2).get

    val loadedConfig = ConfigurationLoader.load(identity, credsV2) {
      case identity: AwsIdentity => SSMConfigurationLocation.default(identity)
      case development: DevIdentity => SSMConfigurationLocation(s"/DEV/flexible/${development.app}", region)
    }

    new AppComponents(
      context.copy(initialConfiguration = Configuration(loadedConfig).withFallback(context.initialConfiguration)),
      region,
      identity,
      creds
    ).application
  }
}
