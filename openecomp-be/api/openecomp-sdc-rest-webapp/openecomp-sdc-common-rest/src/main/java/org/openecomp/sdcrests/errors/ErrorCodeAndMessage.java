package org.openecomp.sdcrests.errors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@NoArgsConstructor
public class ErrorCodeAndMessage {

    /**
     * the HTTP status code.
     */
    private HttpStatus status;
    /**
     * Error code no. if available.
     */
    private String errorCode;
    /**
     * the error message to be displayed.
     */
    private String message;

    /**
     * Instantiates a new Error code and message.
     *
     * @param status    the status
     * @param errorCode the error code
     */
    public ErrorCodeAndMessage(HttpStatus status, ErrorCode errorCode) {
        this.status = status;
        this.message = errorCode.message();
        this.errorCode = errorCode.id();
    }

}
