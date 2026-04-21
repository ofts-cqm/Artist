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

## Note:
- This mod only direct movements and automatically replenish stocks. 
It **DOES NOT** automatically place carpet. Recommended to use along Litematica Printer. 
- This mod is a **15-Color** painting bot, meaning it does not support **Gray**
- This mod is not **Fully Automatic!** Because this mod is still in development, various bugs may occur.
My personal recommendation is to check the status about every 5 minutes. 

# How to use:

You first need a working `.nbt` file. Currently, only `.nbt` format is supported. 
`.litematic` from Litematica will not work and `.schem` from WE are not tested. 

Load the `.nbt` file on your Litematica as usual, then use commands to load the schematic file to this mod. 
This mod does **NOT** automatically sync schematics from Litematica. 

## Commands:

- `/artist load <schematic>`
  - Load the schematic
- `/artist offset`
  - Set the block on your feet to the schematic position. 
  - Note that this command should be run before running `/artist load`
- `/artist query`
  - Query the block on your feet. 
  - This is a debug command that is used to check if you placed your schematics correctly. 
- `/artist target <targets>`
  - This command accepts an array of color and set them to the target blocks. 
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
  - WIP

# Functions:
- When painting, automatically gose to the closest empty block to place. 
- When the player's inventory is out of stock, automatically replenish stock from ender chest via command '/myx'. 
  - Note that this is my server's command to open player's ender chest. Therefore, it probably will not work on your server. 
  - I plant to add this command as a config, but WIP. 
  - When replenishing stocks, automatically disable litematica printer by simulating a keyboard caplock input. 
    - Although this sometimes causes bugs, because it fails to re-enable the printer. 
- Automatically stop when there is more than 5 carpet mismatch (for safety)
- Automatically stop when not enough carpets are provided
- When the painter stopped and minecraft is not on focus, automatically emit a pop-up window to inform the user
  - I tested this on my *ArchLinux+Hyprland* system, and it works. I haven't tested it on Windows nor macOS

# Known Bugs:
- Sometimes the painter fails to re-enable litematica printer
- Sometimes the painter creates a ghost-hotbar, meaning an out-of-sync hotbar. 
  - This might be caused by my 300ms+ latency and 16% package lost rate, tho. 

Because this is mod is still rapidly developing, and it is mainly intended for personal use, I will not create any releases. 
You can build this mod using Gradle if you are interested. 