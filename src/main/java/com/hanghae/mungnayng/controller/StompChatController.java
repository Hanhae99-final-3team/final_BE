package com.hanghae.mungnayng.controller;

import com.hanghae.mungnayng.domain.Room.Dto.RoomInfoResponseDto;
import com.hanghae.mungnayng.domain.Room.Dto.RoomInviteDto;
import com.hanghae.mungnayng.domain.chat.dto.ChatDto;
import com.hanghae.mungnayng.domain.member.Member;
import com.hanghae.mungnayng.service.ChatService;
import com.hanghae.mungnayng.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final SimpMessagingTemplate template; //특정 Broker로 메세지를 전달
    private final ChatService chatService;
    private final RoomService roomService;

    //Client가 SEND할 수 있는 경로
    //stompConfig에서 설정한 applicationDestinationPrefixes와 @MessageMapping 경로가 병합됨
    //"/pub/chat/enter"

    /*사용자 채팅 리스트 불러오기*/
    @MessageMapping(value = "/room/{memberId}")
    public void enter(@DestinationVariable String memberId) {
        List<RoomInfoResponseDto> rooms = roomService.getRoomInfo(memberId);
        log.info("room list");
        template.convertAndSend("/sub/room/" + memberId, rooms);
    }

    @MessageMapping(value = "/sub/room/{roomId}")
    public void message(@DestinationVariable String roomId, ChatDto message) {
        /*채팅 저장*/
        message = chatService.saveChat(Long.parseLong(roomId), message);
        log.info("pub success" + message.getContent());
        template.convertAndSend("/sub/chat/room/" + roomId, message); /*채팅방으로*/
    }

    @MessageMapping(value = "/room/invite/{memberId}")
    public void invite(@DestinationVariable String memberId, RoomInviteDto inviteDto) {
//        Member member = new Member();
        long parsedmemberId = Long.parseLong(memberId);
        RoomInfoResponseDto responseDto = roomService.createRoom(parsedmemberId);/*채팅방 개설*/
        log.info("채팅방 개설 완료");
        template.convertAndSend("/sub/room/invite/" + memberId, responseDto);

        roomService.inviteRoom(parsedmemberId, responseDto.getRoomInfoId(), inviteDto);/*나와 상대를 개설 채팅방으로 입장*/

    }
}