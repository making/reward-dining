package lol.maki.rewarddining.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentageTests {
	@Test
	void testPercentageValueOfString() {
		Percentage percentage = Percentage.valueOf("100%");
		assertEquals("100%", percentage.toString());
	}

	@Test
	void testPercentage() {
		assertEquals(Percentage.valueOf("0.01"), Percentage.valueOf("1%"));
	}

	@Test
	void testPercentageEquality() {
		Percentage percentage1 = Percentage.valueOf("25%");
		Percentage percentage2 = Percentage.valueOf("25%");
		assertEquals(percentage1, percentage2);
	}

	@Test
	void testNewPercentage() {
		Percentage p = new Percentage(.25);
		assertEquals("25%", p.toString());
	}

	@Test
	void testNewPercentageWithRounding() {
		Percentage p = new Percentage(.255555);
		assertEquals("26%", p.toString());
	}
}