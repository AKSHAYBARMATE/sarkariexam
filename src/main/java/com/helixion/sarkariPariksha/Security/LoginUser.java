package com.helixion.sarkariPariksha.Security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
@Scope
@Builder
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long candidateId;
    private String logId;
    private String mobileNumber;
    private String role;
}
