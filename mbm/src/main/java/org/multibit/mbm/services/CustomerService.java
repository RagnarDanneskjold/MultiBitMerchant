package org.multibit.mbm.services;

import com.google.common.base.Optional;
import org.multibit.mbm.db.dao.CartDao;
import org.multibit.mbm.db.dao.CustomerDao;
import org.multibit.mbm.db.dao.ItemDao;
import org.multibit.mbm.db.dto.Cart;
import org.multibit.mbm.db.dto.Customer;
import org.multibit.mbm.db.dto.Item;
import com.yammer.dropwizard.logging.Log;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>Service to provide the following to Controllers:</p>
 * <ul>
 * <li>Transactional collection of Customer entries</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
@Service
@Transactional(readOnly = true)
public class CustomerService {

  private static final Log LOG = Log.forClass(CustomerService.class);

  @Resource(name = "hibernateCustomerDao")
  private CustomerDao customerDao;

  @Resource(name = "hibernateCartDao")
  private CartDao cartDao;

  @Resource(name = "hibernateItemDao")
  private ItemDao itemDao;

  /**
   * Handles the process of updating the quantity of CartItems
   *
   * @param customer The Customer
   * @param itemId   The Item ID
   * @param quantity The quantity to set
   *
   * @return The updated Customer
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public Customer setCartItemQuantity(Customer customer, Long itemId, int quantity) {
    Optional<Cart> cart = cartDao.getInitialisedCartByCustomer(customer);

    // Find the Item in the database
    Optional<Item> item = itemDao.getById(itemId);

    // Set the quantity of Items in the cart
    cart.get().setItemQuantity(item.get(), quantity);

    return customerDao.saveOrUpdate(customer);

  }

  /**
   * Attempts to persist the Customer
   *
   * @param customer The Customer to persist
   *
   * @return The Customer
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public Customer persist(Customer customer) {
    return customerDao.saveOrUpdate(customer);
  }

  /**
   * Package local to allow testing
   *
   * @return The Customer DAO
   */
  CustomerDao getCustomerDao() {
    return customerDao;
  }

  public void setCustomerDao(CustomerDao customerDao) {
    this.customerDao = customerDao;
  }

  public void setCartDao(CartDao cartDao) {
    this.cartDao = cartDao;
  }

  public void setItemDao(ItemDao itemDao) {
    this.itemDao = itemDao;
  }

  public Optional<Customer> findByOpenId(String openId) {
    return customerDao.getCustomerByOpenId(openId);
  }
}