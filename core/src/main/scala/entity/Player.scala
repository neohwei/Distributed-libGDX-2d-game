package my.game.pkg.entity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

import java.util.UUID

import my.game.pkg.entity.utils.Direction
import my.game.pkg.entity.utils.Direction._
import my.game.pkg.entity.utils.State
import my.game.pkg.entity.utils.State._
import my.game.pkg.assets.AssetsManager
import my.game.pkg.map.MapManager

class Player{
	val entityID = UUID.randomUUID().toString()
	val velocity = new Vector2(8f, 8f)
	var currentDirection = Direction.LEFT
	var previousDirection = Direction.UP
	var state = State.IDLE
	var frameTime:Float = 0f
	val nextPlayerPosition = new Vector2()
	val currentPlayerPosition = new Vector2()
	val boundingBox = new Rectangle()

	AssetsManager.loadTextureAsset(Player.defaultSpritePatch)
	val texture:Option[Texture] = AssetsManager.getTextureAsset(Player.defaultSpritePatch)
	val walkDownFrames = new Array[TextureRegion](4)
	val walkLeftFrames = new Array[TextureRegion](4)
	val walkRightFrames = new Array[TextureRegion](4)
	val walkUpFrames = new Array[TextureRegion](4)
	texture match{
		case Some(tex) => 
			for(x <- 0 to 3; val textureFrames:scala.Array[scala.Array[TextureRegion]] = TextureRegion.split(tex, Player.FRAME_WIDTH, Player.FRAME_HEIGHT)){
				for(y <- 0 to 3){
					x match{
						case 0 => walkDownFrames.insert(y, textureFrames(x)(y))
						case 1 => walkLeftFrames.insert(y, textureFrames(x)(y))
						case 2 => walkRightFrames.insert(y, textureFrames(x)(y))
						case 3 => walkUpFrames.insert(y, textureFrames(x)(y))
					}
				}
			}
	}
	val walkDownAnimation = new Animation[TextureRegion](0.25f, walkDownFrames, Animation.PlayMode.LOOP)
	val walkLeftAnimation = new Animation[TextureRegion](0.25f, walkLeftFrames, Animation.PlayMode.LOOP)
	val walkRightAnimation = new Animation[TextureRegion](0.25f, walkRightFrames, Animation.PlayMode.LOOP)
	val walkUpAnimation = new Animation[TextureRegion](0.25f, walkUpFrames, Animation.PlayMode.LOOP)

	var currentFrame = walkDownFrames.get(0)
	val frameSprite = new Sprite(currentFrame.getTexture(), 0, 0, Player.FRAME_WIDTH, Player.FRAME_HEIGHT)

	def update(delta:Float, direction:Direction, currentState:State){
		frameTime = (frameTime + delta)%5
		setBoundingSize(0f, 0.5f)
		previousDirection = currentDirection
		currentDirection = direction
		state = currentState

		velocity.scl(delta)
		if(currentState != State.IDLE){
			currentDirection match{
				case Direction.LEFT => 
					nextPlayerPosition.x = currentPlayerPosition.x - velocity.x
					nextPlayerPosition.y = currentPlayerPosition.y
					currentFrame = walkLeftAnimation.getKeyFrame(frameTime)
				case Direction.RIGHT => 
					nextPlayerPosition.x = currentPlayerPosition.x + velocity.x
					nextPlayerPosition.y = currentPlayerPosition.y
					currentFrame = walkRightAnimation.getKeyFrame(frameTime)
				case Direction.UP => 
					nextPlayerPosition.y = currentPlayerPosition.y + velocity.y
					nextPlayerPosition.x = currentPlayerPosition.x
					currentFrame = walkUpAnimation.getKeyFrame(frameTime)
				case Direction.DOWN => 
					nextPlayerPosition.y = currentPlayerPosition.y - velocity.y
					nextPlayerPosition.x = currentPlayerPosition.x
					currentFrame = walkDownAnimation.getKeyFrame(frameTime)
				case _ => 
			}
		}
		velocity.scl(1 / delta)
	}

	def update(delta:Float, currentState:State){
		update(delta, currentDirection, currentState)
	}

	def init(position:Vector2){
		currentPlayerPosition.x = position.x.toInt
		currentPlayerPosition.y = position.y.toInt
		nextPlayerPosition.x = position.x.toInt
		nextPlayerPosition.y = position.y.toInt
	}

	def setBoundingSize(widthReduce:Float, heightReduce:Float){
		val width = Player.FRAME_WIDTH * (1.0f - widthReduce)
		val height = Player.FRAME_HEIGHT * (1.0f - heightReduce)

		if(width == 0 || height == 0){
			Gdx.app.debug(Player.TAG, s"Width and Height are 0! $width:$height")
		}

		val minX = nextPlayerPosition.x / MapManager.UNIT_SCALE
		val minY = nextPlayerPosition.y / MapManager.UNIT_SCALE

		boundingBox.set(minX, minY, width, height)
	}

	def dispose(){
		AssetsManager.unloadAsset(Player.defaultSpritePatch)
	}

	def move(){
		currentPlayerPosition.x = nextPlayerPosition.x
		currentPlayerPosition.y = nextPlayerPosition.y
		frameSprite.setX(currentPlayerPosition.x)
		frameSprite.setY(currentPlayerPosition.y)
	}
}

object Player{

	def apply():Player = new Player()

	private val TAG:String = Player.getClass().getSimpleName()
	private val defaultSpritePatch:String = "sprites/characters/Warrior.png"
	val FRAME_WIDTH = 16
	val FRAME_HEIGHT = 16
}