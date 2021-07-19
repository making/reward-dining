package lol.maki.rewarddining.account.web;

import java.util.List;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.AccountManager;
import lol.maki.rewarddining.account.StubAccountManager;
import org.junit.jupiter.api.Test;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountControllerTest {
	private static final long VALID_ACCOUNT_ID = 0L;

	private static final long ILLEGAL_ACCOUNT_ID = 10101;

	AccountManager manager = new StubAccountManager();

	AccountController controller = new AccountController(manager);

	@Test
	void getAccountDetails() {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getAccountDetails(VALID_ACCOUNT_ID, model);
		Account account = (Account) model.get("account");
		assertNotNull(account);
		assertEquals(Long.valueOf(0), account.getId());
	}

	@SuppressWarnings("unchecked")
	@Test
	void getAccountList() {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getAccountList(model);
		List<Account> accounts = (List<Account>) model.get("accounts");
		assertNotNull(accounts);
		assertEquals(getNumAccountsExpected(), accounts.size());
		assertEquals(Long.valueOf(0), accounts.get(0).getId());
	}


//	@Test
//	void invalidId() {
//		assertThrows(ObjectRetrievalFailureException.class, () -> {
//			ExtendedModelMap model = new ExtendedModelMap();
//			controller.getAccountDetails(ILLEGAL_ACCOUNT_ID, model);
//		});
//	}

	@Test
	void validateAllValid() {
		Account account = new Account("123456789", "Ben");
		Errors errors = new BindException(account, "account");
		controller.validateAccount(account, errors);
		assertEquals(0, errors.getErrorCount(), "No errors should be registered");
	}

	@Test
	void validateInvalidName() {
		Account account = new Account("1", "");
		Errors errors = new BindException(account, "account");
		controller.validateAccount(account, errors);
		assertEquals(2, errors.getErrorCount(), "One error should be registered");
		FieldError error = errors.getFieldError("name");
		assertNotNull(error, "Should have an error registred for the name field");
		assertEquals("charSequence.notBlank", error.getCode(), "Should have registered an empty value error");
	}

	@Test
	void validateInvalidNumber() {
		Account account = new Account("", "Ben");
		Errors errors = new BindException(account, "account");
		controller.validateAccount(account, errors);
		assertEquals(2, errors.getErrorCount(), "One error should be registered");
		FieldError error = errors.getFieldError("number");
		assertNotNull(error, "Should have an error registred for the number field");
		assertEquals("charSequence.notBlank", error.getCode(), "Should have registered an empty value error");
	}

	@Test
	void validateAllInvalid() {
		Account account = new Account(null, null);
		Errors errors = new BindException(account, "account");
		controller.validateAccount(account, errors);
		assertEquals(2, errors.getErrorCount(), "Two errors should be registered");
	}

	@Test
	void editAccount() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getEditAccount(VALID_ACCOUNT_ID, model);
		Account account = (Account) model.get("account");
		assertNotNull(account);
		assertEquals(Long.valueOf(0), account.getId());
	}

	@Test
	void successfulPost() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getEditAccount(VALID_ACCOUNT_ID, model);
		Account account = (Account) model.get("account");
		account.setName("Ben");
		account.setNumber("987654321");
		BindingResult br = new MapBindingResult(model, "account");
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		String viewName = controller.postEditAccount(account, br, new SimpleSessionStatus(), attributes, model);
		Account modifiedAccount = manager.getAccount(VALID_ACCOUNT_ID);
		assertEquals("Ben", modifiedAccount.getName(), "Object name has not been updated by post");
		assertEquals("987654321", modifiedAccount.getNumber(), "Object number has not been updated by post");
		assertEquals("redirect:/accountDetails", viewName,
				"Post has not redirected to the correct URL");
		assertEquals(String.valueOf(VALID_ACCOUNT_ID), attributes.get("id"));
	}

	@Test
	void unsuccessfulPost() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getEditAccount(VALID_ACCOUNT_ID, model);
		Account account = (Account) model.get("account");
		account.setName("");
		account.setNumber("");
		BindingResult br = new MapBindingResult(model, "account");
		String viewName = controller.postEditAccount(account, br, new SimpleSessionStatus(), new RedirectAttributesModelMap(), model);
		assertEquals("editAccount", viewName, "Invalid Post has not returned to correct view");
	}

	private int getNumAccountsExpected() {
		return StubAccountManager.NUM_ACCOUNTS_IN_STUB;
	}
}