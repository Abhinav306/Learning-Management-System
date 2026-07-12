package com.abhinav.lms.ai.tutor.mapper; 

import com.abhinav.lms.ai.tutor.dto.ChatMessageResponse;
import com.abhinav.lms.ai.tutor.dto.ChatSessionResponse;
import com.abhinav.lms.ai.tutor.entity.AiChatMessage;
import com.abhinav.lms.ai.tutor.entity.AiChatSession;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AiTutorMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    ChatSessionResponse toResponse(AiChatSession session);

    List<ChatSessionResponse> toSessionResponseList(List<AiChatSession> sessions);

    ChatMessageResponse toResponse(AiChatMessage message);

    List<ChatMessageResponse> toMessageResponseList(List<AiChatMessage> messages);
}
