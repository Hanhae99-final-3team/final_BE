package com.hanghae.mungnayng.repository;

import com.hanghae.mungnayng.domain.item.Item;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hanghae.mungnayng.domain.item.QItem.item;

@Repository
@RequiredArgsConstructor
public class ItemQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 상품 리스트 조회 동적 쿼리 메서드(전체 상품 조회 및 카테고리에 의한 상품 조회 메서드 통합)
     */
    public Page<Item> getItemListByCategory(String petCategory, String itemCategory, Pageable pageable) {
        /* content - 조건에 맞는 itemList 반환 */
        List<Item> itemList = jpaQueryFactory.selectFrom(item)
                .where(
                        petCategoryEq(petCategory),
                        itemCategoryEq(itemCategory))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        /* totalCount - 조건에 맞는 itemList를 count */
        long total = jpaQueryFactory.select(Wildcard.count).from(item)
                .where(
                        petCategoryEq(petCategory),
                        itemCategoryEq(itemCategory))
                .fetchFirst();
        /* PageImpl -> Page 인터페이스의 구현체, 인자로 1. content 2. pageable 3. totalCount(content 수)를 받음 */
        return new PageImpl<>(itemList, pageable, total);
    }

    /**
     * 상품 조회 시 해당 상품이 마지막 상품인지 조회하는 메서드(무한 스크롤 구현 위함)
     */
    public Long getLastData(String petCategory, String itemCategory) {
        return jpaQueryFactory.select(item.id.min())
                .from(item)
                .where(
                        petCategoryEq(petCategory),
                        itemCategoryEq(itemCategory))
                .orderBy(item.id.desc())
                .fetchFirst();
    }

    /* BooleanExpression -> where 절에 필요한 조건식 반환
     * null 반환 시 조건(where)절에서 조건이 무시(제거)되어 안전 / 전부 null이면 전체 값 호출 */
    private BooleanExpression petCategoryEq(String petCategory) {
        return petCategory != null ? item.petCategory.eq(petCategory) : null;
    }

    private BooleanExpression itemCategoryEq(String itemCateogry) {
        return itemCateogry != null ? item.itemCategory.eq(itemCateogry) : null;
    }
}
