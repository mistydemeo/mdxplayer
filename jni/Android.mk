
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

FMGEN_DIR   = pmdmini/src/fmgen
PMDWIN_DIR  = pmdmini/src/pmdwin
PMDMINI_DIR = pmdmini/src

MDX_DIR     = mdxmini/src



SRCS_MDX = \
 mdxmini.c \
 mdx2151.c \
 mdxmml_ym2151.c \
 pdxfile.c \
 mdxfile.c \
 pcm8.c \
 ym2151.c

MDX_SRCS = $(addprefix $(MDX_DIR)/,$(SRCS_MDX))

SRCS_FMGEN = \
 file.cpp \
 fmtimer.cpp \
 opna.cpp \
 fmgen.cpp \
 opm.cpp \
 psg.cpp

SRCS_PMDWIN = \
opnaw.cpp \
p86drv.cpp \
pmdwin.cpp \
ppsdrv.cpp \
ppz8l.cpp \
table.cpp \
util.cpp

SRCS_PMDMINI = \
 pmdmini.c


PMD_SRCS = $(addprefix $(FMGEN_DIR)/,$(SRCS_FMGEN))
PMD_SRCS += $(addprefix $(PMDWIN_DIR)/,$(SRCS_PMDWIN))
PMD_SRCS += $(addprefix $(PMDMINI_DIR)/,$(SRCS_PMDMINI))

FMGEN_CFLAGS   = -I$(LOCAL_PATH)/$(FMGEN_DIR)
PMDWIN_CFLAGS  = -I$(LOCAL_PATH)/$(PMDWIN_DIR)
PMDMINI_CFLAGS = -I$(LOCAL_PATH)/$(PMDMINI_DIR)

MDX_CFLAGS    = -I$(LOCAL_PATH)/$(MDX_DIR)

INC_CFLAGS = $(FMGEN_CFLAGS) $(MDX_CFLAGS) $(PMDMINI_CFLAGS)

OPT_FLAG = -O3

LOCAL_CFLAGS    := -g $(INC_CFLAGS) $(OPT_FLAG)
LOCAL_CPPFLAGS  := -g $(INC_CFLAGS) $(OPT_FLAG)

LOCAL_LDLIBS := -llog

LOCAL_MODULE    := mdxmini
LOCAL_SRC_FILES := mdxjni.c $(MDX_SRCS) $(PMD_SRCS)
include $(BUILD_SHARED_LIBRARY)

