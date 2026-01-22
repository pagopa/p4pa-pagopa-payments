package it.gov.pagopa.pu.pagopapayments.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class PageUtilsTest {

  @Test
  void fetchAllFromPaginatedApiTwoPages() {
    List<String> stringList = List.of("string1", "string2", "string3", "string4", "string5", "string6", "string7", "string8");
    List<String> resultList = PageUtils.fetchAllFromPaginatedApi(
      pageNum -> pageNum == 1 ? stringList.stream().limit(5).toList()
        : stringList.stream().skip(5).toList(),
      List::isEmpty,
      l -> 2, // 2 pages
      l -> l // map to identity
    );
    Assertions.assertNotNull(resultList);
    Assertions.assertEquals(stringList.size(), resultList.size());
    Assertions.assertTrue(resultList.containsAll(stringList));
  }

  @Test
  void fetchAllFromPaginatedApiOnePage() {
    List<String> stringList = List.of("string1", "string2", "string3", "string4", "string5");
    List<String> resultList = PageUtils.fetchAllFromPaginatedApi(
      pageNum -> stringList,
      List::isEmpty,
      l -> 1, // 1 pages
      l -> l // map to identity
    );
    Assertions.assertNotNull(resultList);
    Assertions.assertEquals(stringList.size(), resultList.size());
    Assertions.assertTrue(resultList.containsAll(stringList));
  }

  @Test
  void fetchAllFromPaginatedApiZeroPages() {
    List<String> resultList = PageUtils.fetchAllFromPaginatedApi(
      pageNum -> new ArrayList<String>(),
      l -> true, //test for empty page
      l -> 0, // 0 pages
      l -> l // map to identity
    );
    Assertions.assertNotNull(resultList);
    Assertions.assertEquals(0, resultList.size());
  }
}
