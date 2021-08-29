package com.funnywolf.hollowkit.app.fragments.douban

import androidx.annotation.DrawableRes

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/11
 */
class DoubanHeader (
    @DrawableRes var posterRes: Int,
    var title: String,
    var subTitle: String,
    var info: String
)

class DoubanRating (
    var rating: Float,
    var info: String
)

class DoubanTags

class TitleModel(
    var text: String,
    var info: String?
)

class Brief(var text: String)

class Actor(
    @DrawableRes var avatar: Int,
    var name: String,
    var info: String
)

class Actors: ArrayList<Actor>()

class Picture(@DrawableRes val pictureRes: Int)
class Pictures: ArrayList<Picture>()
