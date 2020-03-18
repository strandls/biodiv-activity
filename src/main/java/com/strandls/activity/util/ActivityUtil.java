package com.strandls.activity.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.strandls.activity.pojo.TaggedUser;

public class ActivityUtil {
	
	public static List<TaggedUser> getTaggedUsers(String comment) {
		List<TaggedUser> users = new ArrayList<TaggedUser>(); 
		String regex = "@\\[\\w+\\]\\(\\d+\\)";
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(comment);
        while (matcher.find()) {
        	String match = matcher.group();
        	TaggedUser user = new TaggedUser();
        	user.setName(match.substring(match.indexOf("[") + 1, match.lastIndexOf("]")));
        	user.setName(match.substring(match.indexOf("(") + 1, match.lastIndexOf(")")));
        	users.add(user);
        }        
		return users;
	} 

}
