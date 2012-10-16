package org.multibit.mbm.auth.hmac;

import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import org.multibit.mbm.db.dao.UserDao;
import org.multibit.mbm.db.dto.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <p>Authenticator to provide the following to application:</p>
 * <ul>
 * <li>Verifies the provided credentials are valid</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Component
public class HmacAuthenticator implements Authenticator<HmacCredentials, User> {

  @Resource(name="hibernateUserDao")
  private UserDao userDao;

  @Override
  public Optional<User> authenticate(HmacCredentials credentials) throws AuthenticationException {

    // Get the User referred to by the API key
    Optional<User> user = userDao.getByUUID(credentials.getApiKey());
    if (!user.isPresent()) {
      return Optional.absent();
    }

    // Check that their authorities match their credentials
    if (!user.get().hasAllAuthorities(credentials.getAuthorities())) {
      return Optional.absent();
    }

    // Locate their secret key
    String secretKey = user.get().getSecretKey();

    String computedSignature = new String(
      HmacUtils.computeSignature(
        credentials.getAlgorithm(),
        credentials.getCanonicalRepresentation().getBytes(),
        secretKey.getBytes()));

    // Avoid timing attacks by verifying every byte every time
    if (isEqual(computedSignature.getBytes(), credentials.getDigest().getBytes())) {
      return user;
    }

    return Optional.absent();

  }

  public void setUserDao(UserDao userDao) {
    this.userDao = userDao;
  }

  /**
   * Performs a byte array comparison with a constant time
   *
   * @param a A byte array
   * @param b Another byte array
   * @return True if the byte array have equal contents
   */
  public static boolean isEqual(byte[] a, byte[] b) {
    if (a.length != b.length) {
      return false;
    }

    int result = 0;
    for (int i = 0; i < a.length; i++) {
      result |= a[i] ^ b[i];
    }
    return result == 0;
  }

}