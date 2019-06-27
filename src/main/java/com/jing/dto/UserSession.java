package com.jing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.websocket.Session;
import java.io.Serializable;

/**
 * @author ruanjiayu
 * @dateTime 2019/6/27 13:46
 */
@Data
@AllArgsConstructor
public class UserSession implements Serializable{

    private static final long serialVersionUID = 1056612615853199610L;

    private String username;

    private Session session;
}
