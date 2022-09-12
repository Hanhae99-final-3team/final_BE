package com.hanghae.mungnayng.service;

import com.hanghae.mungnayng.domain.image.Image;
import com.hanghae.mungnayng.domain.item.Item;
import com.hanghae.mungnayng.domain.item.dto.ItemMainResponseDto;;
import com.hanghae.mungnayng.domain.search.ItemSearch;
import com.hanghae.mungnayng.domain.search.dto.ItemSearchResponseDto;
import com.hanghae.mungnayng.repository.*;
import com.hanghae.mungnayng.util.TimeUtil;
import com.hanghae.mungnayng.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SearchService {

    private final ItemRepository itemRepository;
    private final ImageRepository imageRepository;
    private final SearchRepository searchRepository;
    private final Validator validator;

    /* 상품 기본 검색 - 최신순('item - title / content'를 바탕으로) */
    @Transactional
    public List<ItemMainResponseDto> searchItem(UserDetails userDetails, String toggle, String keyword) {
        String nickname = "nonMember";  /* 비회원으로 검색할 경우 nickname은 nonMember로 저장 */
        if (userDetails != null) {
            nickname = (userDetails.getUsername());
        }

        if(toggle.equals("true")) {     /* 검색어 자동저장 토글 on(true)일 경우에만 검색어 저장*/
            ItemSearch itemSearch = ItemSearch.builder()
                    .nickname(nickname)
                    .searchWord(keyword)
                    .build();
            searchRepository.save(itemSearch);
        }

        /* return 값 -> keyword를 바탕으로 상품 찾아 Dto List 작성 */
        List<Item> itemList = itemRepository.getAllItemListByTitleOrContent(keyword);
        List<ItemMainResponseDto> itemMainResponseDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemMainResponseDtoList.add(
                    buildItemMainResponseDto(item)
            );
        }

        return itemMainResponseDtoList;
    }

    /* 상품 기본 검색 - 인기순('item - title / content'를 바탕으로) */
    @Transactional
    public List<ItemMainResponseDto> searchItemOrderByPopularity(UserDetails userDetails, String toggle, String keyword) {
        String nickname = "null";
        if (userDetails != null) {
            nickname = (userDetails.getUsername());
        }

        if(toggle.equals("true")) {     /* 검색어 자동저장 토글 on(true)일 경우에만 검색어 저장*/
            ItemSearch itemSearch = ItemSearch.builder()
                    .nickname(nickname)
                    .searchWord(keyword)
                    .build();
            searchRepository.save(itemSearch);
        }

        /* return 값 -> keyword를 바탕으로 상품 찾아 Dto List 작성 */
        List<Item> itemList = itemRepository.getAllItemListByOrderByPopularity(keyword);
        List<ItemMainResponseDto> itemMainResponseDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemMainResponseDtoList.add(
                    buildItemMainResponseDto(item)
            );
        }

        return itemMainResponseDtoList;
    }

    /* 공통 작업 - ResponseDto build */
    private ItemMainResponseDto buildItemMainResponseDto(Item item) {

        // 해당 item의 이미지 호출
        List<Image> imageList = imageRepository.findAllByItemId(item.getId());
        List<String> imgUrlList = new ArrayList<>();
        for (Image image : imageList) {
            System.out.println();
            imgUrlList.add(image.getImgUrl());
        }

        return ItemMainResponseDto.builder()
                .id(item.getId())
                .title(item.getTitle())
                .petCategory(item.getPetCategory())
                .itemCategory(item.getItemCategory())
                .itemImgs(imgUrlList)
                .location(item.getLocation())
                .zzimCnt(item.getZzimCnt())
                .viewCnt(item.getViewCnt())
                .sellingPrice(item.getSellingPrice())
                .IsComplete(item.isComplete())
                .time(TimeUtil.convertLocaldatetimeToTime(item.getCreatedAt()))
                .build();
    }

    /* 최근 검색어 조회 */
    public List<ItemSearchResponseDto> getSearchWord(UserDetails userDetails) {
        List<ItemSearchResponseDto> itemSearchResponseDtoList = new ArrayList<>();

        if (userDetails == null) {    /* 비로그인 시 빈 배열 출력 */
            return itemSearchResponseDtoList;
        }

        String nickname = (userDetails.getUsername());
        List<String> searchWordList = searchRepository.getAllByNickname(nickname);
        for (String string : searchWordList) {
            itemSearchResponseDtoList.add(
                    ItemSearchResponseDto.builder()
                            .searchWord(string)
                            .build()
            );
        }
        return itemSearchResponseDtoList;
    }

    /* 최근 검색어 개별 삭제*/
    public void deleteSearchWord(UserDetails userDetails, String searchWord) {
        validator.validateUserDetailsInput(userDetails);   /* 로그인 유효성 검사 */

        List<ItemSearch> itemSearchList = searchRepository.getAllSearchWordByNicknameAndSearchWord(userDetails.getUsername(), searchWord);
        searchRepository.deleteAll(itemSearchList);
    }

    /* 최근 검색어 전체 삭제 */
    public void deleteAllSearchWord(UserDetails userDetails) {
        validator.validateUserDetailsInput(userDetails);   /* 로그인 유효성 검사 */

        List<ItemSearch> itemSearchList = searchRepository.getAllSearchWordByNickname(userDetails.getUsername());
        searchRepository.deleteAll(itemSearchList);
    }

    /* 인기 검색어 조회 */
    public List<ItemSearchResponseDto> getPopularSearchWord() {
        List<String> searchwordList = searchRepository.getAllByPopularity();
        List<ItemSearchResponseDto> itemSearchResponseDtoList = new ArrayList<>();
        for (String string : searchwordList) {
            itemSearchResponseDtoList.add(
                    ItemSearchResponseDto.builder()
                            .searchWord(string)
                            .build()
            );
        }
        return itemSearchResponseDtoList;
    }

    /* 검색어 자동완성 */
    public List<ItemSearchResponseDto> getKeywordAutomatically(String keyword) {
        List<String> searchwordList = searchRepository.getSearchwordByKeyword(keyword);
        List<ItemSearchResponseDto> itemSearchResponseDtoList = new ArrayList<>();
        for (String string : searchwordList) {
            itemSearchResponseDtoList.add(
                    ItemSearchResponseDto.builder()
                            .searchWord(string)
                            .build()
            );
        }
        return itemSearchResponseDtoList;
    }
}
