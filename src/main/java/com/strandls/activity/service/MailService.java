package com.strandls.activity.service;

import java.util.List;

import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.pojo.TaggedUser;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;

public interface MailService {

	public void sendMail(MAIL_TYPE type, String objectType, Long objectId, Long userId, CommentLoggingData comment,
			ActivityLoggingData activity, List<TaggedUser> taggedUsers);

}
