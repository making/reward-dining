package lol.maki.rewarddining.restaurant.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;

import lol.maki.rewarddining.account.Percentage;
import lol.maki.rewarddining.restaurant.AlwaysAvailable;
import lol.maki.rewarddining.restaurant.BenefitAvailabilityPolicy;
import lol.maki.rewarddining.restaurant.NeverAvailable;
import lol.maki.rewarddining.restaurant.Restaurant;
import lol.maki.rewarddining.restaurant.RestaurantRepository;
import lol.maki.rewarddining.util.FileLoader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Loads restaurants from a data source using the JDBC API.
 */
@Repository
public class JdbcRestaurantRepository implements RestaurantRepository {

	private final JdbcTemplate jdbcTemplate;

	public JdbcRestaurantRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Maps a row returned from a query of T_RESTAURANT to a Restaurant object.
	 */
	private final RowMapper<Restaurant> rowMapper = new RestaurantRowMapper();

	public Restaurant findByMerchantNumber(String merchantNumber) {
		final String sql = FileLoader.load("lol/maki/rewarddining/restaurant/JdbcRestaurantRepository/findByMerchantNumber.sql");
		return jdbcTemplate.queryForObject(sql, rowMapper, merchantNumber);
	}

	/**
	 * Maps a row returned from a query of T_RESTAURANT to a Restaurant object.
	 *
	 * @param rs the result set with its cursor positioned at the current row
	 */
	private Restaurant mapRestaurant(ResultSet rs) throws SQLException {
		// get the row column data
		String name = rs.getString("NAME");
		String number = rs.getString("MERCHANT_NUMBER");
		Percentage benefitPercentage = Percentage.valueOf(rs.getString("BENEFIT_PERCENTAGE"));
		// map to the object
		Restaurant restaurant = new Restaurant(number, name);
		restaurant.setBenefitPercentage(benefitPercentage);
		restaurant.setBenefitAvailabilityPolicy(mapBenefitAvailabilityPolicy(rs));
		return restaurant;
	}

	/**
	 * Helper method that maps benefit availability policy data in the ResultSet to a fully-configured
	 * {@link BenefitAvailabilityPolicy} object. The key column is 'BENEFIT_AVAILABILITY_POLICY', which is a
	 * discriminator column containing a string code that identifies the type of policy. Currently supported types are:
	 * 'A' for 'always available' and 'N' for 'never available'.
	 *
	 * <p>
	 * More types could be added easily by enhancing this method. For example, 'W' for 'Weekdays only' or 'M' for 'Max
	 * Rewards per Month'. Some of these types might require additional database column values to be configured, for
	 * example a 'MAX_REWARDS_PER_MONTH' data column.
	 *
	 * @param rs the result set used to map the policy object from database column values
	 * @return the matching benefit availability policy
	 * @throws IllegalArgumentException if the mapping could not be performed
	 */
	private BenefitAvailabilityPolicy mapBenefitAvailabilityPolicy(ResultSet rs) throws SQLException {
		String policyCode = rs.getString("BENEFIT_AVAILABILITY_POLICY");
		if ("A".equals(policyCode)) {
			return AlwaysAvailable.INSTANCE;
		}
		else if ("N".equals(policyCode)) {
			return NeverAvailable.INSTANCE;
		}
		else {
			throw new IllegalArgumentException("Not a supported policy code " + policyCode);
		}
	}

	private class RestaurantRowMapper implements RowMapper<Restaurant> {

		public Restaurant mapRow(ResultSet rs, int rowNum) throws SQLException {
			return mapRestaurant(rs);
		}

	}
}