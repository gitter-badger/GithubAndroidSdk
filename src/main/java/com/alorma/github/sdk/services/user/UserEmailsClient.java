package com.alorma.github.sdk.services.user;

import android.content.Context;

import com.alorma.github.sdk.bean.dto.response.ListEmails;

/**
 * Created by Bernat on 12/07/2014.
 */
public class UserEmailsClient extends GithubUsersClient<ListEmails> {
    public UserEmailsClient(Context context) {
        super(context);
    }

    @Override
    protected void executeService(UsersService usersService) {
        usersService.userEmails(this);
    }

}
