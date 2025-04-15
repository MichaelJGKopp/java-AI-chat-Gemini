# Gemini Console Chat

This application allows you to chat with Gemini AI in the console using a free API key from Google AI Studio. 

## Features
- Chat with Gemini AI directly in the console.
- Save chat logs as a Markdown file (`Chat.md`) in the root folder.
- Toggle between a concise response and the full JSON response using the `PRINT_FULL_RESPONSE` flag.

## Setup
1. **Set API Key**:  
   Obtain a free API key from Google AI Studio and set it as an environment variable: GEMINI_API_KEY=<your_api_key>
2. **Branches**:  
   A basic version of the application is available on a separate branch.

## Usage
- Run the application in the console.
- Enter your prompts to chat with Gemini AI.
- Chat logs will be saved in `Chat.md`.

## Configuration
- **Toggle Full Response**:  
  Set `PRINT_FULL_RESPONSE = TRUE` in the code to display the full JSON response instead of the extracted text.
