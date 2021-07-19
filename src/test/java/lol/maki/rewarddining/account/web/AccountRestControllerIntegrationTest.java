package lol.maki.rewarddining.account.web;

import java.net.URI;
import java.util.Random;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.Beneficiary;
import lol.maki.rewarddining.account.Percentage;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = { "management.metrics.export.wavefront.enabled=false", "logging.level.web=DEBUG", "logging.level.sql=TRACE" })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountRestControllerIntegrationTest {
	@Autowired
	TestRestTemplate restTemplate;

	Random random = new Random();

	@Test
	public void listAccounts() {
		String url = "/accounts";
		// we have to use Account[] instead of List<Account>, or Jackson won't know what type to unmarshal to
		Account[] accounts = restTemplate.getForObject(url, Account[].class);
		assertTrue(accounts.length >= 21);
		assertEquals("Keith and Keri Donald", accounts[0].getName());
		assertEquals(2, accounts[0].getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), accounts[0].getBeneficiary("Annabelle").getAllocationPercentage());
	}

	@Test
	public void getAccount() {
		String url = "/accounts/{accountId}";
		Account account = restTemplate.getForObject(url, Account.class, 0);
		assertEquals("Keith and Keri Donald", account.getName());
		assertEquals(2, account.getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), account.getBeneficiary("Annabelle").getAllocationPercentage());
	}

	@Test
	public void createAccount() {
		String url = "/accounts";
		// use a unique number to avoid conflicts
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");
		URI newAccountLocation = restTemplate.postForLocation(url, account);
		System.out.println(newAccountLocation);

		Account retrievedAccount = restTemplate.getForObject(newAccountLocation, Account.class);
		assertEquals(account.getNumber(), retrievedAccount.getNumber());

		Beneficiary accountBeneficiary = account.getBeneficiaries().iterator().next();
		Beneficiary retrievedAccountBeneficiary = retrievedAccount.getBeneficiaries().iterator().next();

		assertEquals(accountBeneficiary.getName(), retrievedAccountBeneficiary.getName());
		assertNotNull(retrievedAccount.getId());
	}

	@Test
	public void addAndDeleteBeneficiary() {
		// perform both add and delete to avoid issues with side effects
		String addUrl = "/accounts/{accountId}/beneficiaries";
		URI newBeneficiaryLocation = restTemplate.postForLocation(addUrl, "David", 1);
		Beneficiary newBeneficiary = restTemplate.getForObject(newBeneficiaryLocation, Beneficiary.class);
		assertEquals("David", newBeneficiary.getName());

		restTemplate.delete(newBeneficiaryLocation);

		ResponseEntity<Beneficiary> responseEntity = restTemplate.getForEntity(newBeneficiaryLocation, Beneficiary.class);
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

}
