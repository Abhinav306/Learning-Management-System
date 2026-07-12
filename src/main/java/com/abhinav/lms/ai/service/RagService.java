package com.abhinav.lms.ai.service;

import com.abhinav.lms.ai.dto.RagQueryRequest;
import com.abhinav.lms.ai.dto.RagQueryResponse;
import com.abhinav.lms.security.model.UserPrincipal;

public interface RagService {
    RagQueryResponse queryRag(RagQueryRequest request, UserPrincipal currentUser);
}
