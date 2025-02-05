<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# json2tablep Changelog

### Added
#### Improvements
- Enhanced thread handling for better compatibility with IntelliJ IDEA 2024.3.2
- Optimized action update mechanism to prevent EDT (Event Dispatch Thread) violations
- Improved file type detection for JSON files
- More robust project and file validation

#### Bug Fixes
- Fixed a critical issue where PSI file access could cause EDT violations
- Resolved potential thread-related crashes in the action update process
- Enhanced error handling for file operations

#### Technical Updates
- Updated action thread model to use Background Thread (BGT) for update operations
- Simplified file extension checking logic
- Improved overall plugin stability and performance