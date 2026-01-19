package it.gov.pagopa.pu.pagopapayments.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class PageUtils {

  private PageUtils() {}

  public static <P,O> List<O> fetchListFromPaginatedResponse(
    IntFunction<P> apiCall,
    Predicate<P> isPageEmpty,
    ToIntFunction<P> pageToTotalPageMapper,
    Function<P, List<O>> pageToListMapper) {
    int pageNum = 1;
    P firstPagedResult = apiCall.apply(pageNum);
    if(isPageEmpty.test(firstPagedResult)) {
      return new ArrayList<>();
    }
    int totPage = pageToTotalPageMapper.applyAsInt(firstPagedResult);
    List<O> resultList = new ArrayList<>(pageToListMapper.apply(firstPagedResult));
    for (pageNum = 2; pageNum <= totPage; pageNum++) {
      P pagedResult = apiCall.apply(pageNum);
      List<O> partialResultList = pageToListMapper.apply(pagedResult);
      resultList.addAll(partialResultList);
    }
    return resultList;
  }

}
