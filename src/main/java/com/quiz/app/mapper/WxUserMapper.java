package com.quiz.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.app.entity.WxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WxUserMapper extends BaseMapper<WxUser> {
    
    @Select("SELECT * FROM wx_user WHERE openid = #{openid}")
    WxUser findByOpenid(@Param("openid") String openid);
    
    @Update("UPDATE wx_user SET nickname = #{nickname}, avatar = #{avatar} WHERE id = #{id}")
    void updateUserInfo(@Param("id") Long id, @Param("nickname") String nickname, @Param("avatar") String avatar);
}
