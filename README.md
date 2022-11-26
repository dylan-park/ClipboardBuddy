# ClipboardBuddy
A lightweight, open-source java applet, meant to supercharge your clipboard experience with configurable search and replace rules.
## Features
- Regex based *Search and Replace* system for modifying your clipboard automatically upon copying
- Configuration is file based, and easy to share
- Automation is toggleable, when necessary
- Clipboard history is preserved locally while app is running
## About
ClipboardBuddy is an extension of the idea conceived in my [TwitterFxAdder](https://github.com/dylan-park/TwitterFxAdder) project. That project was only created to solve a very specific niche problem I was facing, but I realized the concept is pretty easily expanded into a much more fleshed out system.

The primary use-cases I see from this project include (but are not limited to):
- Removing tracking/affiliate query strings from known URLs
- Modifying links from Twitter, YouTube, etc, to utilize web services that better display their content (like TwitFix)
- Redacting known common information from your clipboard
- Managing a local history of clipboard items

## Usage
*(An example rules file is avalible [here](https://github.com/dylan-park/ClipboardBuddy/blob/main/examples/rules.json))*

Upon opening for the first time, a data file will be created at %APPDATA%/ClipboardBuddy

The rules file operates using regex matching groups. Each regex rule should have at least one matching group, and there should be an equal number of replacement strings in the replace JSON Array in the order the matches will appear. The layout of the rules file is a JSON Array of JSON Objects. Each object is formatted like so:

```
{
"name": "Replace https (Example)",
"regex": "(http):\/\/",
"replace": ["https"]
}
```

In this example, any string copied to the clipboard that matches the regex will be considered. Then each matching group will fully replace its contents with the replace string at the matching index. The final result will then be applied to the clipboard. This rule specifically will match any string that begins with *http://*, and will replace *http* with *https*. Using these rule sets you can begin to build more complex search and replace based rules for your  clipboard. 