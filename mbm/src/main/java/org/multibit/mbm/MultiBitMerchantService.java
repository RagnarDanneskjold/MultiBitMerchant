package org.multibit.mbm;

import com.google.common.cache.CacheBuilderSpec;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.logging.Log;
import org.multibit.mbm.auth.hmac.HmacAuthenticator;
import org.multibit.mbm.auth.hmac.HmacCredentials;
import org.multibit.mbm.auth.hmac.HmacRestrictedToProvider;
import org.multibit.mbm.db.dto.User;
import org.multibit.mbm.health.TemplatePropertyHealthCheck;
import org.multibit.mbm.resources.*;
import org.multibit.mbm.resources.admin.AdminCartResource;
import org.multibit.mbm.resources.admin.AdminItemResource;
import org.multibit.mbm.resources.admin.AdminRoleResource;
import org.multibit.mbm.resources.admin.AdminUserResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>Service to provide the following to application:</p>
 * <ul>
 * <li>Provision of access to resources</li>
 * </ul>
 * <p>Use <code>java -jar mbm-develop-SNAPSHOT.jar server mbm.yml</code> to start MBM</p>
 *
 * @since 0.0.1
 *         
 */
public class MultiBitMerchantService extends Service<MultiBitMerchantConfiguration> {

  public static final Log LOG = Log.forClass(MultiBitMerchantService.class);

  /**
   * Main entry point to the application
   *
   * @param args Command line arguments
   *
   * @throws Exception If something goes wrong
   */
  public static void main(String[] args) throws Exception {
    new MultiBitMerchantService().run(args);
  }

  private MultiBitMerchantService() {
    super("mbm");
  }

  @Override
  protected void initialize(MultiBitMerchantConfiguration configuration,
                            Environment environment) {

    LOG.info("Reading configuration");

    // Read the configuration

    // Start Spring context based on the provided location
    // TODO Externalise this into the configuration - Spring provides too much to ignore
    ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
      "/spring/mbm-context.xml"
    });

    // Configure authenticator
    HmacAuthenticator hmacAuthenticator = context.getBean(HmacAuthenticator.class);
    CachingAuthenticator<HmacCredentials, User> cachingAuthenticator = CachingAuthenticator
      .wrap(hmacAuthenticator, CacheBuilderSpec.parse(configuration.getAuthenticationCachePolicy()));

    // Configure environment accordingly
    // Resources - admin (needs ROLE_ADMIN)
    environment.addResource(context.getBean(AdminUserResource.class));
    environment.addResource(context.getBean(AdminRoleResource.class));
    environment.addResource(context.getBean(AdminItemResource.class));
    environment.addResource(context.getBean(AdminCartResource.class));
    // Resources - customer (needs ROLE_CUSTOMER)
    environment.addResource(context.getBean(CustomerUserResource.class));
    environment.addResource(context.getBean(CustomerResource.class));
    environment.addResource(context.getBean(CustomerCartResource.class));
    environment.addResource(context.getBean(BitcoinPaymentResource.class));
    // Resources - public (no authentication)
    environment.addResource(context.getBean(PublicItemResource.class));

    // Health checks
    environment.addHealthCheck(new TemplatePropertyHealthCheck());

    // Providers
    environment.addProvider(new HmacRestrictedToProvider<User>(cachingAuthenticator, "REST"));

    // TODO Add the database loader code here
//    if (configuration.loadInitialData) {
//      new DataBaseLoader.initialise();
//    }

  }


}