# What is this?

This is a Minecraft carpet map art painting bot. 
It supports semi-automatic carpet placing. 

## What is map art?

You should know that in Minecraft we have an art called Pixel Art. 
The problem of pixel art is that you cannot share it with other people freely. 
They have to come to your place to see your art. Therefore, mapart created. 
Mapart is the art ot placing blocks on the ground, then record the blocks with a map. 
The benefits of mapart including the convenience of using map to share your painting 
so other people can see your art anywhere in the server. 

## What is carpet map art?

Carpet map art is a special form of map art. The problem of all pixel art is that the material barely recycled. 
Therefore, people began to use carpet to paint pixel art. Carpet can be destroyed by water and therefore can be easily recycled and gathered. 

## What does this mod do?

Most of the time, we don't paint by ourselves. We generate schematics from various
website and paste the generated schematics in the server. This mod automates the pasting/construction process
by creating a bot that automatically place carpets,
so the player can be freed from this repetitive task and spend their time on more valuable things. 

After placing a schematic down (using Litematica) and storing all materials in 
inventory, Ender chest, or cloud storage, you can `Y` to toggle the bot.
The bot will then automatically build your pixel art, automatically break any wrong
blocks, automatically pick-up any carpets, and automatically replenish supplies. 


## Note:
- This mod is a **15-Color** painting bot, meaning it does not support **Gray**
- This mod is **In Development**, and various bugs may occur.

# How to use:

1. Place a schematic down via Litematica. 
2. Load all required materials in inventory, Ender Chest, or cloud storage
3. Press `Y` **INSIDE** your schematic placement to load and start

Yes, that is the entire process. 
Note that you should press `Y` inside your placement. If you start the bot
outside any placement, the bot will use the last placement, and if the bot cannot find
the last placement, it will stop. 


This bot is fully automatic and does not require 
human oversight during the building process. However, because
this mod is still in development, my personal recommendation is to check its state
every 10 min. 

## Commands:

Commands serves for additional debugging features. You do not need those command to get this bot working. 

- `/artist load [schematic]`
  - Load the schematic
  - If no argument is provided, load the currently schematic
    placement that the player is currently standing on. 
  - **NOTE: This command (the one with argument) is now deprecated!**
    Schematics now will be automatically loaded, 
    according to the schematic placement you are currently in
- `/artist query`
  - Query the block on your feet. 
  - This is a debug command that is used to check if you placed your schematics correctly. 
  - Historically, schematics of this mod needs to be manually separately loaded.
    Therefore, I added this command to check if manual loading is correct. 
    However, since the entire loading process is now automated, there is no need to check again. 
- `/artist target <targets>`
  - This command accepts an array of color and set them to the target blocks. 
  - When new schematic is loaded, the target will automatically set to ALL
  - The acceptable values are (case-sensitive):
    - WHITE
    - LIGHT_GRAY
    - BLACK
    - BLUE
    - LIGHT_BLUE
    - CYAN
    - ORANGE
    - YELLOW
    - RED
    - GREEN
    - LIME
    - MAGENTA
    - PURPLE
    - PINK
    - BROWN
    - ALL
  - You can enter multiple targets. For example, the following command set the targets to white, black, and pink carpets

    - `/artist target WHITE BLACK PINK`
- `/artist state`
  - This is a debug command that prints the current state (i.e, targets and litematics loaded)
- `/artist start` and `/artist stop`:
  - Start and Stop Painting. This can be replaced by the keybind `Y`, which **Toggles** painting. 
- `/artist audit`
  - This command has been deleted. 

# At The End

Because this is mod is still rapidly developing, and it is mainly intended for personal use, I will not create any releases. 
You can build this mod using Gradle if you are interested. 