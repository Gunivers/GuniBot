package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.Member;

public class DataMember extends DataObject<Member> {

	public DataMember(Member member) {
		super(member);
	}

	public DataMember(Member member, JSONObject json) {
		super(member, json);
	}

}
