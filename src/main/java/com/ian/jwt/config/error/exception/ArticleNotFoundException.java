package com.ian.jwt.config.error.exception;

import com.ian.jwt.config.error.ErrorCode;

public class ArticleNotFoundException extends NotFoundException {

    public ArticleNotFoundException() {
        super(ErrorCode.ARTICLE_NOT_FOUND);
    }
}
