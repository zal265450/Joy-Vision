package org.luckyjourney.service.poll;

import org.springframework.beans.factory.InitializingBean;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 01:49
 */
public interface ThreadPoll<T> extends InitializingBean {

    void submit(T t);
}
