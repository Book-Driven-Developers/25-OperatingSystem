# Evidence Map — Chapter 15 File Systems

- **Contiguous allocation**: blocks allocated in contiguous ranges; fast sequential access; fragmentation and resizing issues.  
  - Source: https://en.wikipedia.org/wiki/File_system

- **Linked (chained) allocation**: each file is a linked list of disk blocks; no external fragmentation, random access slow.  
  - Source: https://en.wikipedia.org/wiki/File_system

- **Indexed allocation (i-node / index blocks)**: metadata block contains pointers to data blocks (direct/indirect). Good random access; supports large files via single/double/triple indirect blocks.  
  - Source: https://en.wikipedia.org/wiki/Inode

- **FAT (File Allocation Table)**: table of cluster chains stored in a reserved area of disk; widely used in removable media; similar semantics to linked allocation but table-based.  
  - Source: https://en.wikipedia.org/wiki/File_Allocation_Table

- **Directory structure / metadata**: directory entries map names to inode numbers or FAT entries; permissions and timestamps stored in metadata.  
  - Source: ch15_filesystem_notes/sources/url_a.md

- **Block device simulation notes**: simulate fixed-size blocks, free-block list / bitmap, and allocation strategies to observe fragmentation and access patterns.  
  - Source: practical lab design (this repo)
