package shared.dto;

import org.junit.Assert;
import org.junit.Test;

public class DataTransferObjectTest {

	@Test
	public void test1() {
		Assert.assertEquals(DataTransferObject.escape("my string"), "my string");
		Assert.assertEquals(DataTransferObject.unescape("my string"), "my string");
	}

	@Test
	public void test2() {
		Assert.assertEquals(DataTransferObject.escape("my |string"), "my <<[pi]>>string");
		Assert.assertEquals(DataTransferObject.unescape("my <<[pi]>>string"), "my |string");
	}

	@Test
	public void test3() {
		Assert.assertEquals(DataTransferObject.escape("my \nstring"), "my <<[nl]>>string");
		Assert.assertEquals(DataTransferObject.unescape("my <<[nl]>>string"), "my \nstring");
	}

	@Test
	public void test4() {
		Assert.assertEquals(DataTransferObject.escape("m|y \nst|r\ning"), "m<<[pi]>>y <<[nl]>>st<<[pi]>>r<<[nl]>>ing");
		Assert.assertEquals(DataTransferObject.unescape("m<<[pi]>>y <<[nl]>>st<<[pi]>>r<<[nl]>>ing"), "m|y \nst|r\ning");
	}

}
