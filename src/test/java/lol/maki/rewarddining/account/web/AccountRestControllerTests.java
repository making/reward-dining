package lol.maki.rewarddining.account.web;

import java.util.List;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.Beneficiary;
import lol.maki.rewarddining.account.StubAccountManager;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class AccountRestControllerTests {
	AccountRestController controller = new AccountRestController(new StubAccountManager());


	@Test
	void testHandleDetailsRequest() {
		Account account = controller.accountDetails(0);
		assertNotNull(account);
		assertEquals(Long.valueOf(0), account.getId());
	}

	@Test
	void testHandleSummaryRequest() {
		List<Account> accounts = controller.accountSummary();
		assertNotNull(accounts);
		assertEquals(1, accounts.size());
		assertEquals(Long.valueOf(0), accounts.get(0).getId());
	}

	@Test
	void testCreateAccount() {
		Account newAccount = new Account("11223344", "Test");

		// ServletUriComponentsBuilder expects to find the HttpRequest in the
		// current thread (Spring MVC does this for you). For our test, we need
		// to add a mock request manually
		setupFakeRequest("http://localhost/accounts");

		HttpEntity<?> result = controller.createAccount(newAccount);
		assertNotNull(result);

		// See StubAccountManager.nextEntityId - initialized to 3
		assertEquals("http://localhost/accounts/3", result.getHeaders().getLocation().toString());
	}

	@Test
	void testGetBeneficiary() {
		Beneficiary beneficiary = controller.getBeneficiary(0, "Corgan");
		assertNotNull(beneficiary);
		assertEquals(Long.valueOf(1), beneficiary.getId());
	}

	@Test
	void testAddBeneficiary() {

		// ServletUriComponentsBuilder expects to find the HttpRequest in the
		// current thread (Spring MVC does this for you). For our test, we need
		// to add a mock request manually
		setupFakeRequest("http://localhost/accounts/0/beneficiaries");

		HttpEntity<?> result = controller.addBeneficiary(0L, "Test2");
		assertNotNull(result);
		assertEquals("http://localhost/accounts/0/beneficiaries/Test2", result.getHeaders().getLocation().toString());
	}

	@Test
	void testDeleteBeneficiary() {
		controller.removeBeneficiary(0L, "Corgan");
	}

	@Test
	void testDeleteBeneficiaryFail() {
		try {
			controller.removeBeneficiary(0L, "Fred");
			fail("No such beneficiary 'Fred', " + "IllegalArgumentException expected");
		}
		catch (IllegalArgumentException e) {
			// Expected result
		}
	}

	/**
	 * Add a mocked up HttpServletRequest to Spring's internal request-context
	 * holder. Normally the DispatcherServlet does this, but we must do it
	 * manually to run our test.
	 *
	 * @param url
	 *            The URL we are creating the fake request for.
	 */
	private void setupFakeRequest(String url) {
		String requestURI = url.substring(16); // Drop "http://localhost"

		// We can use Spring's convenient mock implementation. Defaults to
		// localhost in the URL. Since we only need the URL, we don't need
		// to setup anything else in the request.
		MockHttpServletRequest request = new MockHttpServletRequest("POST", requestURI);

		// Puts the fake request in the current thread for the
		// ServletUriComponentsBuilder to initialize itself from later.
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}
}