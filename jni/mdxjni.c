//
// mdxjni
// use ndk-build to build this,
// you need to clean eclipse project to update library.
//


#include <string.h>
#include <stdarg.h>

#include <string.h>
#include <jni.h>
#include <android/log.h>

#include "mdxjni.h"
#include "mdxmini.h"

#include "pmdmini.h"

#include "fade.h"

#define MODE_NONE 0
#define MODE_MDX 1
#define MODE_PMD 2

#define MAX_SIZE 256


int pmdinit_flag = 0;

int op_mode = 0;
t_mdxmini mdx_data;

int render_counter = 0;
int song_len = 0;
int freq = 44100;
char pcmdir[1024];


//
// log output
//
void output_log( const char *format, ... )
{
	char buf[2048];
	va_list arg;
	
	va_start ( arg , format );
	vsprintf( buf , format , arg );
	va_end ( arg );
	
	__android_log_write(ANDROID_LOG_DEBUG,"mdxjni" , buf );
}


/*
 * Class:     com_bkc_android_mdxplayer_PCMRender
 * Method:    sdrv_num_tracks
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1num_1tracks
  (JNIEnv *env, jobject obj)
{
	if ( op_mode == MODE_MDX )
		return mdx_get_tracks ( &mdx_data );
	if ( op_mode == MODE_PMD )
		return pmd_get_tracks();
	
	return 0;
}

/*
 * Class:     com_bkc_android_mdxplayer_PCMRender
 * Method:    sdrv_setpcmdir
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1setpcmdir
  (JNIEnv *env, jobject obj, jstring str)
{
	const char *s = (*env)->GetStringUTFChars(env,str, NULL);
	
	strcpy( pcmdir , s );
	
	(*env)->ReleaseStringUTFChars(env,str, s);
}


/*
 * Class:     com_bkc_android_mdxplayer_PCMRender
 * Method:    sdrv_get_note
 * Signature: ([II)I
 */
JNIEXPORT jint JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1get_1note
  (JNIEnv *env, jobject obj, jintArray data, jint len )
{
	if ( op_mode == MODE_NONE )
	return -1;
	
	
	int *p = (*env)->GetIntArrayElements( env , data , NULL );
	
	if (!p)
		return -1;

    if ( op_mode == MODE_MDX )
		mdx_get_current_notes( &mdx_data , p , len );
	if ( op_mode == MODE_PMD )
		pmd_get_current_notes( p , len );
	

	(*env)->ReleaseIntArrayElements( env , data , p , 0 );

	return 0;
}


//
// sdrv_title
// Returns song title of the file which opened with mdxp_open
//

JNIEXPORT jbyteArray JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1title
  (JNIEnv *env, jobject obj)
{
	jbyteArray ret;
	jbyte *bp;
	
	char title[1024];

	title[0] = 0;
	
	if ( op_mode == MODE_MDX )
		mdx_get_title( &mdx_data , title );

	if ( op_mode == MODE_PMD )
		pmd_get_title( title );
		
	
	ret = (*env)->NewByteArray( env , strlen(title) );

	if (!ret)
		return NULL;

	bp = (*env)->GetByteArrayElements( env , ret , NULL );
	
	if (!bp)
		return NULL;
	
	strcpy(bp,title);
	
	(*env)->ReleaseByteArrayElements( env , ret , bp , 0 );
	
	return ret;
}

//
// sdrv_length
// returns length of the song
//

JNIEXPORT jint JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1length
  (JNIEnv *env, jobject obj)
{
	return song_len;
}


//
// sdrv_setrate
//
JNIEXPORT void JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1setrate
  (JNIEnv *env, jobject obj, jint rate)
{
	freq = rate;
}


//
// sdrv_dofade
//
JNIEXPORT void JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1dofade
  (JNIEnv *env, jobject obj, jint sec)
{
	if (!is_fade_run())
		fade_start( freq , sec );
}

//
// sdrv_is_faded
//

JNIEXPORT jboolean JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1isfaded
  (JNIEnv *env, jobject obj)
{
	if (is_fade_end())
		return JNI_TRUE;
	
	return JNI_FALSE;
}


//
// sdrv_open
// opens file 
//

JNIEXPORT jboolean JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1open
  (JNIEnv *env, jobject obj, jstring path)
{
	int err;
	
	// kick double
	if ( op_mode )
		return JNI_TRUE;
		
	if ( !pmdinit_flag )
	{
		pmd_init();
		pmdinit_flag = 1;
	}
	
	const char *file = (*env)->GetStringUTFChars( env , path , NULL );
	
	if ( pmd_is_pmd( file ) )
	{
		op_mode = MODE_PMD;
		pmd_setrate ( freq );
		err = pmd_play ( file , pcmdir ); 
	}
	else
	{
		op_mode = MODE_MDX;
		mdx_set_rate ( freq );

		err = mdx_open( &mdx_data , (char *)file , pcmdir );
	}
	
	output_log("freq = %d MAX_SIZE=%d DATE:%s TIME:%s",freq,MAX_SIZE,__DATE__,__TIME__);
	
	(*env)->ReleaseStringUTFChars( env , path , file );
	
	
	if ( op_mode == MODE_MDX )
	{
		song_len = mdx_get_length( &mdx_data );
		mdx_set_max_loop( &mdx_data , 0 );
		
			
	}
	if ( op_mode == MODE_PMD )
		song_len = ( pmd_length_sec() + pmd_loop_sec() );
		
	
	
	fade_init();
	
	
	if (err)
	{
		op_mode = MODE_NONE;
		return JNI_TRUE;		
	}

	return JNI_FALSE;
}

//
// sdrv_close 
// close the song
//


JNIEXPORT void JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1close
  (JNIEnv *env , jobject obj)
{
	if ( op_mode == MODE_MDX )
		mdx_stop ( &mdx_data );
		
	if ( op_mode == MODE_PMD )
		pmd_stop();

	op_mode = MODE_NONE;
}

//
// sdrv_render
// gets samples
//

JNIEXPORT void JNICALL Java_com_bkc_android_mdxplayer_PCMRender_sdrv_1render
  (JNIEnv *env, jobject obj, jshortArray data, jint samples)
{
	int pos = 0;
	int left_samples = 0;
	
	if ( op_mode == MODE_NONE )
		return;
	
	
	short *sp = (*env)->GetShortArrayElements( env , data , NULL );
	
	if (!sp)
		return;
	
	// rendering...
	
	left_samples = samples;
	pos = 0;
	
	while( left_samples > 0 )
	{
		// output_log("render_counter : %d\n",render_counter++);
		if ( left_samples > MAX_SIZE )
		{
			if ( op_mode == MODE_MDX )
				mdx_calc_sample( &mdx_data , sp + pos , MAX_SIZE );
			if ( op_mode == MODE_PMD )
				pmd_renderer ( sp + pos , MAX_SIZE );
				
			if (is_fade_run())
				fade_stereo(sp + pos , MAX_SIZE );
			
			left_samples -= MAX_SIZE;
			pos += MAX_SIZE * 2; // stereo

		}
		else 
		{
			if ( op_mode == MODE_MDX )
				mdx_calc_sample( &mdx_data , sp + pos , left_samples );

			if ( op_mode == MODE_PMD )
				pmd_renderer ( sp + pos , left_samples );
				
			if (is_fade_run())
				fade_stereo(sp + pos , left_samples );

			left_samples = 0;
		}
	}
	
	(*env)->ReleaseShortArrayElements( env , data , sp , 0 );
}
